package de.rwth.swc.interact.rest.observer

import com.fasterxml.jackson.core.JacksonException
import de.rwth.swc.interact.domain.*
import de.rwth.swc.interact.observer.TestObserver
import de.rwth.swc.interact.rest.StringRestMessage
import io.github.projectmapk.jackson.module.kogera.jacksonObjectMapper
import org.reactivestreams.Publisher
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpMethod
import org.springframework.http.client.reactive.ClientHttpRequest
import org.springframework.http.client.reactive.ClientHttpRequestDecorator
import org.springframework.http.server.PathContainer
import org.springframework.web.reactive.function.BodyInserter
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.util.pattern.PathPattern
import org.springframework.web.util.pattern.PathPatternParser
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.jvm.optionals.getOrElse

class WebClientObserver(private val isTestHarness: Boolean) : ExchangeFilterFunction {

    override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
        val interfaceUrl = request
            .attribute("org.springframework.web.reactive.function.client.WebClient.uriTemplate").getOrElse {
                request.url()
            }.toString()
        val pathVariables = getPathVariables(interfaceUrl, request.url().toString())?.uriVariables
        val method = request.method()
        val headers = request.headers().entries
        val originalBodyInserter: BodyInserter<*, in ClientHttpRequest?> = request.body()

        val loggingClientRequest = ClientRequest.from(request)
            .body { outputMessage, context ->
                val loggingOutputMessage =
                    RequestObserverDecorator(outputMessage, isTestHarness, interfaceUrl, pathVariables, method, headers)
                originalBodyInserter.insert(loggingOutputMessage, context)
            }
            .build()
        val result = next.exchange(loggingClientRequest)
            .map { clientResponse: ClientResponse ->
                clientResponse.mutate()
                    .body { f ->
                        f.mapNotNull { dataBuffer ->
                            val body = dataBuffer.toString(StandardCharsets.UTF_8)
                            ObservationHelper.recordResponse(
                                isTestHarness,
                                interfaceUrl,
                                pathVariables,
                                method,
                                headers,
                                body
                            )
                            dataBuffer
                        }
                    }
                    .build()
            }

        return result
    }

    private fun getPathVariables(interfaceUrl: String, url: String): PathPattern.PathMatchInfo? {
        val parser = PathPatternParser()
        val pathPattern = parser.parse(interfaceUrl)
        val container = PathContainer.parsePath(url)
        return pathPattern.matchAndExtract(container)
    }


    private class RequestObserverDecorator(
        val request: ClientHttpRequest,
        val isTestHarness: Boolean,
        val interfaceUrl: String,
        val pathVariables: MutableMap<String, String>?,
        val httpMethod: HttpMethod,
        val headers: MutableSet<MutableMap.MutableEntry<String, MutableList<String>>>,
    ) : ClientHttpRequestDecorator(request) {
        private val alreadyLogged = AtomicBoolean(false)
        override fun writeWith(body: Publisher<out DataBuffer>): Mono<Void> {
            var copiedBody = body
            val needToLog = alreadyLogged.compareAndSet(false, true)
            if (needToLog) {
                copiedBody = DataBufferUtils.join(copiedBody)
                    .doOnNext { content ->
                        val responseBody = content.toString(StandardCharsets.UTF_8)
                        ObservationHelper.recordRequest(
                            isTestHarness,
                            interfaceUrl,
                            pathVariables,
                            httpMethod,
                            headers,
                            responseBody
                        )
                    }
            }
            return super.writeWith(copiedBody)
        }

        override fun setComplete(): Mono<Void> { // This is for requests with no body (e.g. GET).
            val needToLog = alreadyLogged.compareAndSet(false, true)
            if (needToLog) {
                //No body to log
            }
            return super.setComplete()
        }
    }

    object ObservationHelper {
        private val mapper = jacksonObjectMapper()

        fun recordRequest(
            isTestHarness: Boolean,
            interfaceUrl: String,
            pathVariables: MutableMap<String, String>?,
            method: HttpMethod,
            headers: MutableSet<MutableMap.MutableEntry<String, MutableList<String>>>,
            body: String
        ) {

            val message = StringRestMessage(
                pathVariables ?: mutableMapOf(),
                headers,
                if (isValidJson(body)) body else "\"$body\""
            )

            if (!isTestHarness) {
                TestObserver.recordMessage(
                    SentMessage(
                        MessageType.Sent.COMPONENT_RESPONSE,
                        MessageValue(mapper.writeValueAsString(message)),
                        OutgoingInterface(
                            Protocol("REST"),
                            ProtocolData(
                                mapOf(
                                    Pair("url", interfaceUrl),
                                    Pair("method", method.name()),
                                    Pair("request", "true")
                                )
                            )
                        )
                    )
                )
            } else {
                TestObserver.recordMessage(
                    ReceivedMessage(
                        MessageType.Received.STIMULUS,
                        MessageValue(mapper.writeValueAsString(message)),
                        IncomingInterface(
                            Protocol("REST"),
                            ProtocolData(
                                mapOf(
                                    Pair("url", interfaceUrl),
                                    Pair("method", method.name()),
                                    Pair("request", "true")
                                )
                            )
                        )
                    )
                )
            }
        }

        fun recordResponse(
            isTestHarness: Boolean,
            interfaceUrl: String,
            pathVariables: MutableMap<String, String>?,
            method: HttpMethod,
            headers: MutableSet<MutableMap.MutableEntry<String, MutableList<String>>>,
            body: String
        ) {
            val message = StringRestMessage(
                pathVariables ?: mutableMapOf(),
                headers,
                if (isValidJson(body)) body else "\"$body\""
            )
            if (!isTestHarness) {
                TestObserver.recordMessage(
                    ReceivedMessage(
                        MessageType.Received.ENVIRONMENT_RESPONSE,
                        MessageValue(mapper.writeValueAsString(message)),
                        IncomingInterface(
                            Protocol("REST"),
                            ProtocolData(
                                mapOf(
                                    Pair("url", interfaceUrl),
                                    Pair("method", method.name()),
                                    Pair("request", "false")
                                )
                            )
                        )
                    )
                )
            } else {
                TestObserver.recordMessage(
                    SentMessage(
                        MessageType.Sent.COMPONENT_RESPONSE,
                        MessageValue(mapper.writeValueAsString(message)),
                        OutgoingInterface(
                            Protocol("REST"),
                            ProtocolData(
                                mapOf(
                                    Pair("url", interfaceUrl),
                                    Pair("method", method.name()),
                                    Pair("request", "false")
                                )
                            )
                        )
                    )
                )
            }
        }

        private fun isValidJson(body: String): Boolean {
            try {
                mapper.readTree(body)
            } catch (e: JacksonException) {
                return false
            }
            return true
        }
    }
}

