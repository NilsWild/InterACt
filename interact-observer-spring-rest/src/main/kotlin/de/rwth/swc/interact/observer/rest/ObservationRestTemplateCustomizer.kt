package de.rwth.swc.interact.observer.rest

import org.springframework.boot.web.client.RestTemplateCustomizer
import org.springframework.http.client.BufferingClientHttpRequestFactory
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

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