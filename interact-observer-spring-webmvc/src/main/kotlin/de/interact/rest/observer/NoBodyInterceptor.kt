package de.interact.rest.observer

import jakarta.servlet.DispatcherType
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.handler.MappedInterceptor

@Component
class NoBodyInterceptor(private val loggingService: ObservationService) : HandlerInterceptor {

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        if (DispatcherType.REQUEST.name
                 == request.dispatcherType.name && (request.method == HttpMethod.GET.name() || request.contentLength == 0)
        ) {
            loggingService.logRequest(request, "")
        }

        return true
    }
}

@Configuration
class InterceptorConfig{
    @Bean
    fun logInterceptorMappedInterceptor(noBodyInterceptor: NoBodyInterceptor) = MappedInterceptor(null, noBodyInterceptor)
}