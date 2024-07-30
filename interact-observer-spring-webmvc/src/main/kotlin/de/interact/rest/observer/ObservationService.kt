package de.interact.rest.observer

import de.interact.domain.rest.RestMessage
import de.interact.domain.serialization.SerializationConstants
import de.interact.domain.shared.Protocol
import de.interact.domain.shared.ProtocolData
import de.interact.domain.testobservation.config.Configuration
import de.interact.domain.testobservation.model.IncomingInterface
import de.interact.domain.testobservation.model.MessageValue
import de.interact.domain.testobservation.model.OutgoingInterface
import de.interact.utils.Logging
import de.interact.utils.logger
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerMapping
import java.util.*

@Component
class ObservationService(): Logging {

    val log = logger()
    val urlTemplateMap: MutableMap<String, String> = HashMap()

    fun logRequest(httpServletRequest: HttpServletRequest, body: Any?) {
        val parameters = buildParametersMap(httpServletRequest)

        val message = RestMessage.Request(
            httpServletRequest.requestURI,
            parameters,
            buildHeadersMap(httpServletRequest),
            body
        )

        if(Configuration.observationManager!!.getCurrentTestCase().observedBehavior.messageSequence.size == 0){
            Configuration.observationManager!!.getCurrentTestCase().observedBehavior.addStimulus(
                MessageValue(SerializationConstants.mapper.writeValueAsString(message)),
                IncomingInterface(
                    Protocol("REST"),
                    ProtocolData(mapOf(
                        "method" to httpServletRequest.method.toString(),
                        "path" to httpServletRequest.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE).toString(),
                        "request" to "true"
                    ))
                )
            )
        } else {
            Configuration.observationManager!!.getCurrentTestCase().observedBehavior.addEnvironmentResponse(
                MessageValue(SerializationConstants.mapper.writeValueAsString(message)),
                IncomingInterface(
                    Protocol("REST"),
                    ProtocolData(mapOf(
                        "method" to httpServletRequest.method.toString(),
                        "path" to httpServletRequest.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE).toString(),
                        "request" to "true"
                    ))
                )
            )
        }

        urlTemplateMap[httpServletRequest.requestURI] =
            httpServletRequest.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE).toString()

        val stringBuilder = StringBuilder()
        stringBuilder.append("REQUEST ")
        stringBuilder.append("method=[").append(httpServletRequest.method).append("] ")
        stringBuilder.append("path=[").append(httpServletRequest.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)).append("] ")
        stringBuilder.append("headers=[").append(buildHeadersMap(httpServletRequest)).append("] ")

        if (parameters.isNotEmpty()) {
            stringBuilder.append("parameters=[").append(parameters).append("] ")
        }

        if (body != null) {
            stringBuilder.append("body=[$body]")
        }

        log.info(stringBuilder.toString())
    }

    fun logResponse(
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse,
        body: Any?
    ) {
        val parameters = buildParametersMap(httpServletRequest)
        val message = RestMessage.Response(
            httpServletRequest.requestURI,
            parameters,
            buildHeadersMap(httpServletRequest),
            body,
            httpServletResponse.status
        )

        Configuration.observationManager!!.getCurrentTestCase().observedBehavior.addComponentResponse(
            MessageValue(SerializationConstants.mapper.writeValueAsString(message)),
            OutgoingInterface(
                Protocol("REST"),
                ProtocolData(mapOf(
                    "method" to httpServletRequest.method.toString(),
                    "path" to urlTemplateMap[httpServletRequest.requestURI].toString(),
                    "request" to "false"
                ))
            )
        )

        val stringBuilder = StringBuilder()

        stringBuilder.append("RESPONSE ")
        stringBuilder.append("method=[").append(httpServletRequest.method).append("] ")
        stringBuilder.append("path=[").append(httpServletRequest.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)).append("] ")
        stringBuilder.append("responseHeaders=[").append(buildHeadersMap(httpServletResponse)).append("] ")
        stringBuilder.append("responseBody=[").append(body).append("] ")

        log.info(stringBuilder.toString())
    }

    private fun buildParametersMap(httpServletRequest: HttpServletRequest): Map<String, String> {
        val resultMap: MutableMap<String, String> = HashMap()
        val parameterNames = httpServletRequest.parameterNames

        while (parameterNames.hasMoreElements()) {
            val key = parameterNames.nextElement()
            val value = httpServletRequest.getParameter(key)
            resultMap[key] = value
        }

        return resultMap
    }

    private fun buildHeadersMap(request: HttpServletRequest): Map<String, String> {
        val map: MutableMap<String, String> = HashMap()

        val headerNames: Enumeration<*> = request.headerNames
        while (headerNames.hasMoreElements()) {
            val key = headerNames.nextElement() as String
            val value = request.getHeader(key)
            map[key] = value
        }
        map.remove("traceparent")
        map.remove("host")
        return map
    }

    private fun buildHeadersMap(response: HttpServletResponse): Map<String, String> {
        val map: MutableMap<String, String> = HashMap()

        val headerNames = response.headerNames
        for (header in headerNames) {
            map[header] = response.getHeader(header)
        }

        return map
    }
}