package de.rwth.swc.interact.integrator.rest

import de.rwth.swc.interact.domain.*
import de.rwth.swc.interact.integrator.Integrator
import de.rwth.swc.interact.test.ExITConfiguration
import de.rwth.swc.interact.test.UriFilter
import de.rwth.swc.interact.utils.Logging
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

/**
 * Interceptor for the RestTemplate to replace the responses of the environment of the component under test.
 */
@Order(Ordered.LOWEST_PRECEDENCE)
class RestTemplateIntegrationInterceptor(private val uriFilter: UriFilter) : ClientHttpRequestInterceptor, Logging {

    private val logger = logger()

    @Throws(IOException::class, IllegalStateException::class)
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        val response = execution.execute(request, body)
        when (ExITConfiguration.mode) {
            TestMode.UNIT -> {
                return response
            }
            TestMode.INTERACTION -> {
                val respBody = response.extractBodyAsString()
                val message = ReceivedMessage(
                    MessageType.Received.ENVIRONMENT_RESPONSE,
                    MessageValue(respBody),
                    IncomingInterface(
                        Protocol("REST"),
                        ProtocolData(
                            mapOf(
                                Pair("url", uriFilter.filter(request.uri)),
                                Pair("request", "false")
                            )
                        )
                    )
                )
                val newResponse = Integrator.getReplacement(
                    message
                )?.also { logger.info("Replaced: $respBody with: ${it.value}") } ?: message

                return object : ClientHttpResponse {
                    override fun getHeaders(): HttpHeaders {
                        return response.headers
                    }

                    @Throws(IOException::class)
                    override fun getBody(): InputStream {
                        return ByteArrayInputStream(newResponse.value.value.toByteArray(StandardCharsets.UTF_8))
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
            }
            else -> {
                throw IllegalStateException("Unknown test mode: ${ExITConfiguration.mode}")
            }
        }
    }
}

/**
 * Interceptor for the RestTemplate to replace the requests triggered through the test harness.
 */
@Order(Ordered.LOWEST_PRECEDENCE - 1)
class TestRestTemplateIntegrationInterceptor(private val uriFilter: UriFilter) : ClientHttpRequestInterceptor, Logging {

    private val logger = logger()

    @Throws(IOException::class)
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        when (ExITConfiguration.mode) {
            TestMode.UNIT -> {
                return execution.execute(request, body)
            }
            TestMode.INTERACTION -> {
                val message = ReceivedMessage(
                    MessageType.Received.STIMULUS,
                    MessageValue(String(body, StandardCharsets.UTF_8)),
                    IncomingInterface(
                        Protocol("REST"),
                        ProtocolData(
                            mapOf(
                                Pair("url", uriFilter.filter(request.uri)),
                                Pair("request", "true")
                            )
                        )
                    )
                )
                val newMessage = Integrator.getReplacement(
                    message
                )?.also { logger.info("Replaced: ${String(body, StandardCharsets.UTF_8)} with ${it.value}") } ?: message

                return execution.execute(request, newMessage.value.value.toByteArray(StandardCharsets.UTF_8))
            }
            else -> {
                throw IllegalStateException("Unknown test mode: ${ExITConfiguration.mode}")
            }
        }
    }
}

fun ClientHttpResponse.extractBodyAsString(): String {
    val isr = InputStreamReader(
        this.body, StandardCharsets.UTF_8
    )
    return BufferedReader(isr).lines().collect(Collectors.joining("\n"))
}