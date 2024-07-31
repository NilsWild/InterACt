package de.interact.rest.observer

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.io.UnsupportedEncodingException

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
class InteractResponseBodyFilter(
    private val loggingService: ObservationService
) : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val responseWrapper = ContentCachingResponseWrapper(response)
        val requestWrapper = ContentCachingRequestWrapper(request)

        filterChain.doFilter(requestWrapper, responseWrapper)
        val responseBody: String = getStringValue(
            responseWrapper.contentAsByteArray,
            response.characterEncoding
        )
        loggingService.logResponse(requestWrapper, responseWrapper, responseBody)
        responseWrapper.copyBodyToResponse()
    }

    private fun getStringValue(contentAsByteArray: ByteArray, characterEncoding: String): String {
        try {
            return String(contentAsByteArray, 0, contentAsByteArray.size, charset(characterEncoding))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return ""
    }
}