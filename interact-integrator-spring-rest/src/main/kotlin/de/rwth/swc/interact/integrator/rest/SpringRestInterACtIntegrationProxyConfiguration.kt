package de.rwth.swc.interact.integrator.rest

import de.rwth.swc.interact.test.StripUriFilter
import de.rwth.swc.interact.test.UriFilter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuration class to provide the necessary beans for the Spring Rest InterACT Integrator.
 */
@Configuration
class SpringRestInterACtIntegrationProxyConfiguration {

    @Bean
    @ConditionalOnMissingBean(UriFilter::class)
    fun uriFilter(): UriFilter {
        return StripUriFilter()
    }

    @Bean
    fun testRestTemplateIntegrationInterceptor(uriFilter: UriFilter): TestRestTemplateIntegrationInterceptor {
        return TestRestTemplateIntegrationInterceptor(uriFilter)
    }

    @Bean
    fun restTemplateIntegrationInterceptor(uriFilter: UriFilter): RestTemplateIntegrationInterceptor {
        return RestTemplateIntegrationInterceptor(uriFilter)
    }

    @Bean
    fun integrationRestTemplateCustomizer(interceptor: RestTemplateIntegrationInterceptor): IntegrationRestTemplateCustomizer {
        return IntegrationRestTemplateCustomizer(interceptor)
    }

}