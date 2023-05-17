package de.rwth.swc.interact.integrator.rest

import de.rwth.swc.interact.integrator.Integrator
import de.rwth.swc.interact.integrator.domain.MessageData
import de.rwth.swc.interact.test.ExITConfiguration
import de.rwth.swc.interact.test.UriFilter
import de.rwth.swc.interact.utils.Logging
import de.rwth.swc.interact.utils.TestMode
import de.rwth.swc.interact.utils.logger
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatusCode
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import java.io.*
import java.nio.charset.StandardCharsets
import java.util.stream.Collectors


@Order(Ordered.LOWEST_PRECEDENCE)
class RestTemplateIntegrationInterceptor(private val uriFilter: UriFilter) : ClientHttpRequestInterceptor, Logging {

    private val logger = logger()

    @Throws(IOException::class)
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        if (ExITConfiguration.mode == TestMode.INTERACTION) {
            val response = execution.execute(request, body)
            val isr = InputStreamReader(
                response.body, StandardCharsets.UTF_8
            )
            val respBody = BufferedReader(isr).lines()
                .collect(Collectors.joining("\n"))
            val md = MessageData(
                "REST",
                mapOf(
                    Pair("url", uriFilter.filter(request.uri.toString())),
                    Pair("request", "false")
                ),
                respBody
            )
            var newResponse = Integrator.getReplacement(
                md
            )
            if (newResponse == null) {
                newResponse = md
            } else {
                logger.info("Replaced: $respBody with: ${newResponse.value}")
            }
            return object : ClientHttpResponse {
                override fun getHeaders(): HttpHeaders {
                    return response.headers
                }

                @Throws(IOException::class)
                override fun getBody(): InputStream {
                    return ByteArrayInputStream(newResponse.value.toByteArray(StandardCharsets.UTF_8))
                }

                @Throws(IOException::class)
                override fun getStatusCode(): HttpStatusCode {
                    return response.statusCode
                }

                @Deprecated("Use getStatusCode()", ReplaceWith("response.statusCode.value()"))
                @Throws(IOException::class)
                override fun getRawStatusCode(): Int {
                    @Suppress("DEPRECATION")
                    return response.rawStatusCode
                }

                @Throws(IOException::class)
                override fun getStatusText(): String {
                    return response.statusText
                }

                override fun close() {}
            }
        } else {
            return execution.execute(request, body)
        }
    }
}

@Order(Ordered.LOWEST_PRECEDENCE - 1)
class TestRestTemplateIntegrationInterceptor(private val uriFilter: UriFilter) : ClientHttpRequestInterceptor, Logging {

    private val logger = logger()

    @Throws(IOException::class)
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        if (ExITConfiguration.mode == TestMode.INTERACTION) {
            val md = MessageData(
                "REST",
                mapOf(
                    Pair("url", uriFilter.filter(request.uri.toString())),
                    Pair("request", "true")
                ),
                String(body, StandardCharsets.UTF_8),
            )
            var newMessageData = Integrator.getReplacement(
                md
            )
            if(newMessageData == null){
                newMessageData = md
            } else {
                logger.info("Replaced: ${String(body, StandardCharsets.UTF_8)} with ${newMessageData.value}")
            }
            return execution.execute(request, newMessageData.value.toByteArray(StandardCharsets.UTF_8))
        } else {
            return execution.execute(request, body)
        }
    }
}