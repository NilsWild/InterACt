package de.interact.controller.expectations.specification

import de.interact.domain.expectations.specification.api.ExpectationsCollectionsManagementService
import de.interact.domain.expectations.specification.events.SpecificationEvent
import de.interact.domain.expectations.specification.spi.EventPublisher
import de.interact.domain.expectations.specification.spi.ExpectationsCollections
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ExpectationsSpecificationConfig {

    @Bean
    fun specificationEventPublisher(applicationEventPublisher: ApplicationEventPublisher): EventPublisher {
        return object: EventPublisher {
            override fun publish(event: SpecificationEvent) {
                applicationEventPublisher.publishEvent(event)
            }
        }
    }

    @Bean
    fun expectationsCollectionsManagementService(
        expectationsCollections: ExpectationsCollections,
        eventPublisher: EventPublisher
    ): ExpectationsCollectionsManagementService {
        return ExpectationsCollectionsManagementService(
            expectationsCollections,
            eventPublisher
        )
    }

}