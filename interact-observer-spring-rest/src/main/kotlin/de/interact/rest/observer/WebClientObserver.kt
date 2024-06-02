package de.interact.rest.observer

import com.fasterxml.jackson.core.JacksonException
import de.interact.domain.serialization.SerializationConstants
import de.interact.domain.shared.Protocol
import de.interact.domain.shared.ProtocolData
import de.interact.domain.testobservation.config.Configuration
import de.interact.domain.testobservation.model.*
import de.interact.domain.testobservation.spi.MessageObserver
import de.interact.rest.StringRestMessage
import de.interact.rest.toMultiMap
import de.interact.utils.Logging
import de.interact.utils.MultiMap
import de.interact.utils.logger
import org.reactivestreams.Publisher
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpMethod
import org.springframework.http.client.reactive.ClientHttpRequest
import org.springframework.http.client.reactive.ClientHttpRequestDecorator
import org.springframework.web.reactive.function.BodyInserter
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.jvm.optionals.getOrElse

class WebClientObserver(private val isTestHarness: Boolean) : MessageObserver, ExchangeFilterFunction {

    override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
        val interfaceUrl = request
            .attribute("org.springframework.web.reactive.function.client.WebClient.uriTemplate").getOrElse {
                request.url()
            }.toString()
        val pathVariables = PathVariableExtractor.extractPathVariablesFromUrl(
            interfaceUrl,
            request.url()
        )?.uriVariables?.entries?.map { it.value } ?: emptyList()
        val method = request.method()
        val headers = request.headers()
        val originalBodyInserter: BodyInserter<*, in ClientHttpRequest?> = request.body()

        val loggingClientRequest = ClientRequest.from(request)
            .body { outputMessage, context ->
                val loggingOutputMessage =
                    RequestObserverDecorator(
                        outputMessage,
                        isTestHarness,
                        interfaceUrl,
                        pathVariables,
                        method,
                        headers.toMultiMap()
                    )
                originalBodyInserter.insert(loggingOutputMessage, context)
            }
            .build()
        val result = next.exchange(loggingClientRequest)
            .flatMap { clientResponse: ClientResponse ->
                clientResponse.bodyToMono(String::class.java).flatMap { body ->
                    RestObservationHelper.recordResponse(
                        isTestHarness,
                        interfaceUrl,
                        pathVariables,
                        method,
                        clientResponse.headers().asHttpHeaders().toMultiMap(),
                        body
                    )
                    Mono.just(clientResponse.mutate().body(body).build())
                }
            }
        return result
    }

    private class RequestObserverDecorator(
        val request: ClientHttpRequest,
        val isTestHarness: Boolean,
        val interfaceUrl: String,
        val pathVariables: List<String>,
        val httpMethod: HttpMethod,
        val headers: MultiMap<String, String>,
    ) : ClientHttpRequestDecorator(request) {
        private val alreadyLogged = AtomicBoolean(false)
        override fun writeWith(body: Publisher<out DataBuffer>): Mono<Void> {
            var copiedBody = body
            val needToLog = alreadyLogged.compareAndSet(false, true)
            if (needToLog) {
                copiedBody = DataBufferUtils.join(copiedBody)
                    .doOnNext { content ->
                        val responseBody = content.toString(StandardCharsets.UTF_8)
                        RestObservationHelper.recordRequest(
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
                RestObservationHelper.recordRequest(
                    isTestHarness,
                    interfaceUrl,
                    pathVariables,
                    httpMethod,
                    headers,
                    ""
                )
            }
            return super.setComplete()
        }
    }

    object RestObservationHelper : Logging {
        private val log = logger()
        private val mapper = SerializationConstants.messageMapper

        fun recordRequest(
            isTestHarness: Boolean,
            interfaceUrl: String,
            pathVariables: List<String>,
            method: HttpMethod,
            headers: MultiMap<String, String>,
            body: String
        ) {

            headers.clear("traceparent")
            val message = StringRestMessage(
                pathVariables,
                headers,
                if (isValidJson(body)) body else "\"$body\""
            )

            if (!isTestHarness) {
                Configuration.observationManager!!.getCurrentTestCase().observedBehavior.addComponentResponse(
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
                ).also {
                    log.info("Observed ${ComponentResponseMessage::class.java.simpleName}: $it")
                }
            } else {
                Configuration.observationManager!!.getCurrentTestCase().observedBehavior.addStimulus(
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
                ).also {
                    log.info("Observed ${StimulusMessage::class.java.simpleName}: $it")
                }
            }
        }

        fun recordResponse(
            isTestHarness: Boolean,
            interfaceUrl: String,
            pathVariables: List<String>,
            method: HttpMethod,
            headers: MultiMap<String, String>,
            body: String
        ) {
            headers.clear("traceparent")
            val message = StringRestMessage(
                pathVariables,
                headers,
                if (isValidJson(body)) body else "\"$body\""
            )
            if (!isTestHarness) {
                Configuration.observationManager!!.getCurrentTestCase().observedBehavior.addEnvironmentResponse(
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
                ).also {
                    log.info("Observed ${EnvironmentResponseMessage::class.java.simpleName}: $it")
                }
            } else {
                Configuration.observationManager!!.getCurrentTestCase().observedBehavior.addComponentResponse(
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
                ).also {
                    log.info("Observed ${ComponentResponseMessage::class.java.simpleName}: $it")
                }
            }
        }

        private fun isValidJson(body: String): Boolean {
            if (body.isBlank()) return false
            try {
                mapper.readTree(body)
            } catch (e: JacksonException) {
                return false
            }
            return true
        }
    }

    override fun isFinished(): Boolean {
        return true
    }

}
