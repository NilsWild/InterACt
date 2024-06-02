package de.interact.controller.expectations.derivation

import de.interact.domain.expectations.derivation.api.InteractionExpectationDerivationService
import de.interact.domain.expectations.derivation.events.DerivationEvent
import de.interact.domain.expectations.derivation.spi.EventPublisher
import de.interact.domain.expectations.derivation.spi.Interactions
import de.interact.domain.expectations.derivation.spi.SystemInteractionExpectations
import de.interact.domain.expectations.derivation.spi.UnitTestBasedInteractionExpectations
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ExpectationsDerivationConfig {

    @Bean
    fun derivationEventPublisher(applicationEventPublisher: ApplicationEventPublisher): EventPublisher {
        return object: EventPublisher {
            override fun publish(event: DerivationEvent) {
                applicationEventPublisher.publishEvent(event)
            }
        }
    }

    @Bean
    fun interactionExpectationDerivationService(
        interactions: Interactions,
        unitTestBasedInteractionExpectations: UnitTestBasedInteractionExpectations,
        systemInteractionExpectations: SystemInteractionExpectations,
        eventPublisher: EventPublisher
    ): InteractionExpectationDerivationService {
        return InteractionExpectationDerivationService(
            interactions,
            unitTestBasedInteractionExpectations,
            systemInteractionExpectations,
            eventPublisher
        )
    }

}