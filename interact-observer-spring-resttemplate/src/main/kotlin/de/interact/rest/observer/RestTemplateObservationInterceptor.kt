package de.interact.rest.observer

import de.interact.domain.rest.RestMessage
import de.interact.domain.serialization.SerializationConstants
import de.interact.domain.shared.Protocol
import de.interact.domain.shared.ProtocolData
import de.interact.domain.testobservation.config.Configuration
import de.interact.domain.testobservation.model.IncomingInterface
import de.interact.domain.testobservation.model.MessageValue
import de.interact.domain.testobservation.model.OutgoingInterface
import de.interact.domain.testtwin.abstracttest.concretetest.message.ComponentResponseMessage
import de.interact.domain.testtwin.abstracttest.concretetest.message.EnvironmentResponseMessage
import de.interact.utils.Logging
import de.interact.utils.logger
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.util.UriComponentsBuilder
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.stream.Collectors

@Order(Ordered.LOWEST_PRECEDENCE - 1)
class RestTemplateObservationInterceptor : ClientHttpRequestInterceptor, Logging {

    private val log = logger()

    @Throws(IOException::class)
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        log.info(
            "SEND ${ComponentResponseMessage::class.java.simpleName}: " + request.method + " : " + request.uri.path + " : " + String(
                body,
                StandardCharsets.UTF_8
            )
        )
        val parameters = UriComponentsBuilder.fromUri(request.uri).build().queryParams.map { it.key to it.value.joinToString(",") }.toMap()
        val headers = request.headers.map { it.key to it.value.joinToString(",") }.toMap()
        val message = RestMessage.Request(
            request.uri.path,
            parameters,
            headers,
            String(
                body,
                StandardCharsets.UTF_8
            )
        )
        Configuration.observationManager!!.getCurrentTestCase().observedBehavior.addComponentResponse(
            MessageValue(SerializationConstants.mapper.writeValueAsString(message)),
            OutgoingInterface(
                Protocol("REST"),
                ProtocolData(mapOf(
                    "method" to request.method.toString(),
                    "path" to request.uri.path,
                    "request" to "true"
                ))
            )
        )
        val response = execution.execute(request, body)
        val isr = InputStreamReader(
            response.body, StandardCharsets.UTF_8
        )
        val respBody = BufferedReader(isr).lines()
            .collect(Collectors.joining("\n"))
        val responseHeaders = response.headers.map { it.key to it.value.joinToString(",") }.toMap()
        val responseMessage = RestMessage.Response(
            request.uri.path,
            parameters,
            responseHeaders,
            body,
            response.statusCode.value()
        )
        Configuration.observationManager!!.getCurrentTestCase().observedBehavior.addEnvironmentResponse(
            MessageValue(SerializationConstants.mapper.writeValueAsString(responseMessage)),
            IncomingInterface(
                Protocol("REST"),
                ProtocolData(mapOf(
                    "method" to request.method.toString(),
                    "path" to request.uri.path,
                    "request" to "false"
                ))
            )
        )
        log.info("RECEIVED ${EnvironmentResponseMessage::class.java.simpleName}: $respBody")
        return response
    }
}