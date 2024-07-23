package de.interact.feign

import com.fasterxml.jackson.core.JacksonException
import de.interact.domain.rest.RestMessage
import de.interact.domain.serialization.SerializationConstants
import de.interact.domain.shared.Protocol
import de.interact.domain.shared.ProtocolData
import de.interact.domain.testobservation.config.Configuration
import de.interact.domain.testobservation.model.*
import feign.Logger
import feign.Request
import feign.Response
import feign.Util
import org.slf4j.LoggerFactory
import java.io.IOException

class FeignObserver : Logger() {

    private var log = LoggerFactory.getLogger(FeignObserver::class.java)

    @Throws(IOException::class)
    override fun logAndRebufferResponse(
        configKey: String,
        logLevel: Level,
        response: Response,
        elapsedTime: Long
    ): Response {
        val request = response.request()
        logRequest(request)
        val rebufferedResponse = logResponse(response)

        return super.logAndRebufferResponse(configKey, logLevel, rebufferedResponse, elapsedTime)
    }

    private fun logRequest(request: Request) {
        val interfaceUrlTemplate = request.requestTemplate().path()
        val queryParameters = request.requestTemplate().queries().toMap().mapValues { it.value.joinToString(",") }
        val method = request.httpMethod().name
        val headers = request.headers().toMap().mapValues { it.value.joinToString(",") }
        val body = String(request.body(), request.charset())

        val message = RestMessage.Request(
            interfaceUrlTemplate,
            queryParameters,
            headers,
            if (isValidJson(body)) body else "\"$body\""
        )

        Configuration.observationManager!!.getCurrentTestCase().observedBehavior.addComponentResponse(
            MessageValue(SerializationConstants.messageMapper.writeValueAsString(message)),
            OutgoingInterface(
                Protocol("REST"),
                ProtocolData(
                    mapOf(
                        Pair("path", interfaceUrlTemplate),
                        Pair("method", method),
                        Pair("request", "true")
                    )
                )
            )
        ).also {
            log.info("Observed ${ComponentResponseMessage::class.java.simpleName}: $it")
        }
    }

    private fun logResponse(response: Response): Response {
        val interfaceUrlTemplate = response.request().requestTemplate().path()
        val queryParameters = response.request().requestTemplate().queries().toMap().mapValues { it.value.joinToString(",") }
        val method = response.request().httpMethod().name
        val headers = response.headers().toMap().mapValues { it.value.joinToString(",") }
        val bodyBytes = Util.toByteArray(response.body().asInputStream())
        val body = String(bodyBytes, response.charset())
        val statusCode = response.status()

        val message = RestMessage.Response(
            interfaceUrlTemplate,
            queryParameters,
            headers,
            if (isValidJson(body)) body else "\"$body\"",
            statusCode
        )

        Configuration.observationManager!!.getCurrentTestCase().observedBehavior.addEnvironmentResponse(
            MessageValue(SerializationConstants.messageMapper.writeValueAsString(message)),
            IncomingInterface(
                Protocol("REST"),
                ProtocolData(
                    mapOf(
                        Pair("path", interfaceUrlTemplate),
                        Pair("method", method),
                        Pair("request", "false")
                    )
                )
            )
        ).also {
            log.info("Observed ${EnvironmentResponseMessage::class.java.simpleName}: $it")
        }
        return response.toBuilder().body(bodyBytes).build()
    }

    private fun isValidJson(body: String): Boolean {
        if (body.isBlank()) return false
        try {
            SerializationConstants.messageMapper.readTree(body)
        } catch (e: JacksonException) {
            return false
        }
        return true
    }

    override fun log(configKey: String, format: String, vararg args: Any) {
        log.debug(format(configKey, format, args))
    }

    private fun format(configKey: String, format: String, vararg args: Any): String {
        return String.format(methodTag(configKey) + format, *args)
    }
}