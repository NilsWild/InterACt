package de.rwth.swc.interact.integrator.rest

import org.springframework.boot.web.client.RestTemplateCustomizer
import org.springframework.http.client.BufferingClientHttpRequestFactory
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

/**
 * This class is used to customize the @link{RestTemplate}s provided by the @link{RestTemplateBuilder}.
 * It uses a @link{BufferingClientHttpRequestFactory} to capture the body of each request
 * and adds the @link{RestTemplateIntegrationInterceptor} to the RestTemplate.
 */
class IntegrationRestTemplateCustomizer(private val interceptor: RestTemplateIntegrationInterceptor) :
    RestTemplateCustomizer {
    override fun customize(restTemplate: RestTemplate) {
        if (restTemplate.requestFactory !is BufferingClientHttpRequestFactory) {
            restTemplate.requestFactory = BufferingClientHttpRequestFactory(SimpleClientHttpRequestFactory())
        }
        if (restTemplate.interceptors.stream()
                .noneMatch { it is RestTemplateIntegrationInterceptor }
        ) {
            restTemplate.interceptors.add(interceptor)
        }
    }
}