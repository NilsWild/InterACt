package de.rwth.swc.interact.observer.rest

import de.rwth.swc.interact.domain.*
import de.rwth.swc.interact.observer.TestObserver
import de.rwth.swc.interact.test.UriFilter
import de.rwth.swc.interact.utils.Logging
import de.rwth.swc.interact.utils.logger
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.stream.Collectors

/**
 * Interceptor for the RestTemplate to observe the requests and responses triggered by the component under test.
 */
@Order(Ordered.LOWEST_PRECEDENCE - 1)
class RestTemplateObservationInterceptor(private val uriFilter: UriFilter) : ClientHttpRequestInterceptor, Logging {

    private val logger = logger()

    @Throws(IOException::class)
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        logRequest(request, body)
        val url = uriFilter.filter(request.uri)
        recordRequest(url, body)
        val response = execution.execute(request, body)
        val respBody = response.extractBodyAsString()
        recordResponse(url, respBody)
        logResponse(respBody)
        return response
    }

    private fun logResponse(respBody: String?) {
        logger.info("RECEIVED ${MessageType.Received.ENVIRONMENT_RESPONSE}: $respBody")
    }

    private fun recordResponse(
        url: String,
        respBody: String
    ): String {
        TestObserver.recordMessage(
            ReceivedMessage(
                MessageType.Received.ENVIRONMENT_RESPONSE,
                MessageValue(respBody),
                IncomingInterface(
                    Protocol("REST"),
                    ProtocolData(
                        mapOf(
                            Pair("url", url),
                            Pair("request", "false")
                        )
                    )
                )
            )
        )
        return respBody
    }

    private fun extractBody(response: ClientHttpResponse): String {
        val isr = InputStreamReader(
            response.body, StandardCharsets.UTF_8
        )
        return BufferedReader(isr).lines().collect(Collectors.joining("\n"))
    }

    private fun recordRequest(url: String, body: ByteArray) {
        TestObserver.recordMessage(
            SentMessage(
                MessageType.Sent.COMPONENT_RESPONSE,
                MessageValue(String(body, StandardCharsets.UTF_8)),
                OutgoingInterface(
                    Protocol("REST"),
                    ProtocolData(
                        mapOf(
                            Pair("url", url),
                            Pair("request", "true")
                        )
                    )
                )
            )
        )
    }

    private fun logRequest(request: HttpRequest, body: ByteArray) {
        logger.info(
            "SEND ${MessageType.Sent.COMPONENT_RESPONSE}: " + request.method + " : " + request.uri + " : " + String(
                body,
                StandardCharsets.UTF_8
            )
        )
    }

}

/**
 * Interceptor for the RestTemplate to observe the requests and responses triggered through the test harness.
 */
@Order(Ordered.LOWEST_PRECEDENCE)
class TestRestTemplateObservationInterceptor(private val uriFilter: UriFilter) : ClientHttpRequestInterceptor, Logging {

    private val logger = logger()

    @Throws(IOException::class)
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        logRequest(request, body)
        val url = uriFilter.filter(request.uri)
        recordRequest(url, body)
        val response = execution.execute(request, body)
        val respBody = response.extractBodyAsString()
        recordResponse(url, respBody)
        logResponse(respBody)
        return response
    }

    private fun logResponse(respBody: String) {
        logger.info("SEND ${MessageType.Sent.COMPONENT_RESPONSE}: $respBody")
    }

    private fun recordResponse(url: String, respBody: String) {
        TestObserver.recordMessage(
            SentMessage(
                MessageType.Sent.COMPONENT_RESPONSE,
                MessageValue(respBody),
                OutgoingInterface(
                    Protocol("REST"),
                    ProtocolData(
                        mapOf(
                            Pair("url", url),
                            Pair("request", "false")
                        )
                    )
                )
            )
        )
    }

    private fun logRequest(request: HttpRequest, body: ByteArray) {
        logger.info(
            "RECEIVED ${MessageType.Received.STIMULUS}: " + request.method + " : " + request.uri + " : " + String(
                body,
                StandardCharsets.UTF_8
            )
        )
    }

    private fun recordRequest(url: String, body: ByteArray) {
        TestObserver.recordMessage(
            ReceivedMessage(
                MessageType.Received.STIMULUS,
                MessageValue(String(body, StandardCharsets.UTF_8)),
                IncomingInterface(
                    Protocol("REST"),
                    ProtocolData(
                        mapOf(
                            Pair("url", url),
                            Pair("request", "true")
                        )
                    )
                )
            )
        )
    }
}

fun ClientHttpResponse.extractBodyAsString(): String {
    val isr = InputStreamReader(
        this.body, StandardCharsets.UTF_8
    )
    return BufferedReader(isr).lines().collect(Collectors.joining("\n"))
}