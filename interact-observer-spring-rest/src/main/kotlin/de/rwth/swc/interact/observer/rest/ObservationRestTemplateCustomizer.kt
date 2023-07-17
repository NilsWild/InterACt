package de.rwth.swc.interact.observer.rest

import org.springframework.boot.web.client.RestTemplateCustomizer
import org.springframework.http.client.BufferingClientHttpRequestFactory
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

/**
 * This class is used to customize the @link{RestTemplate}s provided by the @link{RestTemplateBuilder}.
 * It uses a @link{BufferingClientHttpRequestFactory} to capture the body of each request
 * and adds the @link{RestTemplateObservationInterceptor} to the RestTemplate.
 */
class ObservationRestTemplateCustomizer(private val interceptor: RestTemplateObservationInterceptor) :
    RestTemplateCustomizer {
    override fun customize(restTemplate: RestTemplate) {
        if (restTemplate.requestFactory !is BufferingClientHttpRequestFactory) {
            restTemplate.requestFactory = BufferingClientHttpRequestFactory(SimpleClientHttpRequestFactory())
        }
        if (restTemplate.interceptors.stream()
                .noneMatch { it is RestTemplateObservationInterceptor }
        ) {
            restTemplate.interceptors.add(interceptor)
        }
    }
}