package de.interact.rest.observer

import com.fasterxml.jackson.core.JacksonException
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
import java.net.URI
import java.util.*

@Component
class ObservationService: Logging {

    val log = logger()

    fun logRequest(httpServletRequest: HttpServletRequest, body: Any?) {
        val parameters = buildParametersMap(httpServletRequest)
        val pathVariables = PathVariableExtractor.extractPathVariablesFromUrl(
            httpServletRequest.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE).toString(),
            URI(httpServletRequest.requestURI)
        )?.uriVariables?.entries?.map { it.value } ?: emptyList()

        val message = StringRestMessage(
            pathVariables,
            parameters,
            buildHeadersMap(httpServletRequest),
            if (isValidJson(body.toString())) "$body" else "\"$body\""
        )

        if(Configuration.observationManager!!.getCurrentTestCase().observedBehavior.messageSequence.size == 0){
            Configuration.observationManager!!.getCurrentTestCase().observedBehavior.addStimulus(
                MessageValue(SerializationConstants.messageMapper.writeValueAsString(message)),
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
                MessageValue(SerializationConstants.messageMapper.writeValueAsString(message)),
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
        val pathVariables = PathVariableExtractor.extractPathVariablesFromUrl(
            httpServletRequest.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE).toString(),
            URI(httpServletRequest.requestURI)
        )?.uriVariables?.entries?.map { it.value } ?: emptyList()
        val message = StringRestMessage(
            pathVariables,
            parameters,
            buildHeadersMap(httpServletRequest),
            if (isValidJson(body.toString())) "$body" else "\"$body\""
        )

        Configuration.observationManager!!.getCurrentTestCase().observedBehavior.addComponentResponse(
            MessageValue(SerializationConstants.messageMapper.writeValueAsString(message)),
            OutgoingInterface(
                Protocol("REST"),
                ProtocolData(mapOf(
                    "method" to httpServletRequest.method.toString(),
                    "path" to httpServletRequest.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE).toString(),
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

    private fun isValidJson(body: String): Boolean {
        if (body.isBlank()) return false
        try {
            SerializationConstants.messageMapper.readTree(body)
        } catch (e: JacksonException) {
            return false
        }
        return true
    }
}