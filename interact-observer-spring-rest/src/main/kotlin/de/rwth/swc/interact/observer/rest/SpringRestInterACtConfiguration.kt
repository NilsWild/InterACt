package de.rwth.swc.interact.observer.rest

import de.rwth.swc.interact.test.StripUriFilter
import de.rwth.swc.interact.test.UriFilter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

/**
 * Configuration class to provide the necessary beans for the Spring Rest InterACT Observer.
 */
@TestConfiguration
class SpringRestInterACtConfiguration {

    @Bean
    @ConditionalOnMissingBean(UriFilter::class)
    fun uriFilter(): UriFilter {
        return StripUriFilter()
    }

    @Bean
    fun testRestTemplateObservationInterceptor(uriFilter: UriFilter): TestRestTemplateObservationInterceptor {
        return TestRestTemplateObservationInterceptor(uriFilter)
    }

    @Bean
    fun restTemplateObservationInterceptor(uriFilter: UriFilter): RestTemplateObservationInterceptor {
        return RestTemplateObservationInterceptor(uriFilter)
    }

    @Bean
    fun observationRestTemplateCustomizer(interceptor: RestTemplateObservationInterceptor): ObservationRestTemplateCustomizer {
        return ObservationRestTemplateCustomizer(interceptor)
    }

}