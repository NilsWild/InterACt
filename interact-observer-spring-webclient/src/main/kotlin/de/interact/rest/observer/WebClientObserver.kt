package de.interact.rest.observer

import com.fasterxml.jackson.core.JacksonException
import de.interact.domain.rest.RestMessage
import de.interact.domain.serialization.SerializationConstants
import de.interact.domain.shared.Protocol
import de.interact.domain.shared.ProtocolData
import de.interact.domain.testobservation.config.Configuration
import de.interact.domain.testobservation.model.*
import de.interact.domain.testobservation.spi.MessageObserver
import de.interact.rest.toMultiMap
import de.interact.utils.Logging
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
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.jvm.optionals.getOrElse

class WebClientObserver : MessageObserver, ExchangeFilterFunction {

    override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
        val interfaceUrlTemplate = request
            .attribute("org.springframework.web.reactive.function.client.WebClient.uriTemplate").getOrElse {
                request.url().path
            }.toString()
        val interfaceUrl = request.url().path

        val queryParameters = UriComponentsBuilder.fromUri(request.url()).build().queryParams.toMap().mapValues { it.value.joinToString(",") }

        val method = request.method()
        val headers = request.headers()
        val originalBodyInserter: BodyInserter<*, in ClientHttpRequest?> = request.body()

        val loggingClientRequest = ClientRequest.from(request)
            .body { outputMessage, context ->
                val loggingOutputMessage =
                    RequestObserverDecorator(
                        outputMessage,
                        interfaceUrlTemplate,
                        queryParameters,
                        interfaceUrl,
                        method,
                        headers.toMultiMap().getMap().mapValues { it.value.joinToString(",") }
                    )
                originalBodyInserter.insert(loggingOutputMessage, context)
            }
            .build()
        val result = next.exchange(loggingClientRequest)
            .flatMap { clientResponse: ClientResponse ->
                clientResponse.bodyToMono(String::class.java).flatMap { body ->
                    RestObservationHelper.recordResponse(
                        interfaceUrlTemplate,
                        queryParameters,
                        interfaceUrl,
                        method,
                        clientResponse.headers().asHttpHeaders().toMultiMap().getMap().mapValues { it.value.joinToString(",") },
                        body,
                        clientResponse.statusCode().value()
                    )
                    Mono.just(clientResponse.mutate().body(body).build())
                }
            }
        return result
    }

    private class RequestObserverDecorator(
        request: ClientHttpRequest,
        val interfaceUrl: String,
        val queryParameters: Map<String, String>,
        val path: String,
        val httpMethod: HttpMethod,
        val headers: Map<String, String>,
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
                            interfaceUrl,
                            queryParameters,
                            path,
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
                    interfaceUrl,
                    queryParameters,
                    path,
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
        private val mapper = SerializationConstants.mapper

        fun recordRequest(
            interfaceUrl: String,
            queryParameters: Map<String, String>,
            path: String,
            method: HttpMethod,
            headers: Map<String, String>,
            body: String
        ) {

            var prunedHeaders = headers.minus("traceparent")
            val message = RestMessage.Request(
                path,
                queryParameters,
                prunedHeaders,
                if (isValidJson(body)) body else "\"$body\""
            )


            Configuration.observationManager!!.getCurrentTestCase().observedBehavior.addComponentResponse(
                MessageValue(mapper.writeValueAsString(message)),
                OutgoingInterface(
                    Protocol("REST"),
                    ProtocolData(
                        mapOf(
                            Pair("path", interfaceUrl),
                            Pair("method", method.name()),
                            Pair("request", "true")
                        )
                    )
                )
            ).also {
                log.info("Observed ${ComponentResponseMessage::class.java.simpleName}: $it")
            }
        }

        fun recordResponse(
            interfaceUrl: String,
            queryParameters: Map<String, String>,
            path: String,
            method: HttpMethod,
            headers: Map<String, String>,
            body: String,
            statusCode: Int
        ) {
            var prunedHeaders = headers.minus("traceparent")
            val message = RestMessage.Response(
                path,
                queryParameters,
                prunedHeaders,
                if (isValidJson(body)) body else "\"$body\"",
                statusCode
            )

            Configuration.observationManager!!.getCurrentTestCase().observedBehavior.addEnvironmentResponse(
                MessageValue(mapper.writeValueAsString(message)),
                IncomingInterface(
                    Protocol("REST"),
                    ProtocolData(
                        mapOf(
                            Pair("path", interfaceUrl),
                            Pair("method", method.name()),
                            Pair("request", "false")
                        )
                    )
                )
            ).also {
                log.info("Observed ${EnvironmentResponseMessage::class.java.simpleName}: $it")
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
