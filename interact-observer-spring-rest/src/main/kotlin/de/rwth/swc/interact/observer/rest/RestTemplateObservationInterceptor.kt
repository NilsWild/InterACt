package de.rwth.swc.interact.observer.rest

import de.rwth.swc.interact.observer.TestObserver
import de.rwth.swc.interact.observer.domain.ObservedMessage
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

@Order(Ordered.LOWEST_PRECEDENCE - 1)
class RestTemplateObservationInterceptor(private val uriFilter: UriFilter) : ClientHttpRequestInterceptor, Logging {

    private val logger = logger()

    @Throws(IOException::class)
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        logger.info(
            "SEND ${ObservedMessage.Type.COMPONENT_RESPONSE}: " + request.method + " : " + request.uri + " : " + String(
                body,
                StandardCharsets.UTF_8
            )
        )
        TestObserver.recordMessage(
            ObservedMessage(
                "REST",
                mapOf(
                    Pair("url", uriFilter.filter(request.uri.toString())),
                    Pair("request", "true")
                ),
                ObservedMessage.Type.COMPONENT_RESPONSE,
                String(body, StandardCharsets.UTF_8),
                false
            )
        )
        val response = execution.execute(request, body)
        val isr = InputStreamReader(
            response.body, StandardCharsets.UTF_8
        )
        val respBody = BufferedReader(isr).lines()
            .collect(Collectors.joining("\n"))
        TestObserver.recordMessage(
            ObservedMessage(
                "REST",
                mapOf(
                    Pair("url", uriFilter.filter(request.uri.toString())),
                    Pair("request", "false")
                ),
                ObservedMessage.Type.ENVIRONMENT_RESPONSE,
                respBody,
                false
            )
        )
        logger.info("RECEIVED ${ObservedMessage.Type.ENVIRONMENT_RESPONSE}: $respBody")
        return response
    }
}

@Order(Ordered.LOWEST_PRECEDENCE)
class TestRestTemplateObservationInterceptor(private val uriFilter: UriFilter) : ClientHttpRequestInterceptor, Logging {

    private val logger = logger()

    @Throws(IOException::class)
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        logger.info(
            "RECEIVED ${ObservedMessage.Type.STIMULUS}: " + request.method + " : " + request.uri + " : " + String(
                body,
                StandardCharsets.UTF_8
            )
        )
        TestObserver.recordMessage(
            ObservedMessage(
                "REST",
                mapOf(
                    Pair("url", uriFilter.filter(request.uri.toString())),
                    Pair("request", "true")
                ),
                ObservedMessage.Type.STIMULUS,
                String(body, StandardCharsets.UTF_8),
                false
            )
        )
        val response = execution.execute(request, body)
        val isr = InputStreamReader(
            response.body, StandardCharsets.UTF_8
        )
        val respBody = BufferedReader(isr).lines()
            .collect(Collectors.joining("\n"))
        TestObserver.recordMessage(
            ObservedMessage(
                "REST",
                mapOf(
                    Pair("url", uriFilter.filter(request.uri.toString())),
                    Pair("request", "false")
                ),
                ObservedMessage.Type.COMPONENT_RESPONSE,
                respBody,
                false
            )
        )
        logger.info("SEND ${ObservedMessage.Type.COMPONENT_RESPONSE}: $respBody")
        return response
    }
}