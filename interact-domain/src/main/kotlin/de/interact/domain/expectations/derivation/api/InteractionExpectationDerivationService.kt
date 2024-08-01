package de.interact.domain.expectations.derivation.api

import de.interact.domain.expectations.derivation.events.InteractionExpectationAddedEvent
import de.interact.domain.expectations.derivation.interactionexpectation.InteractionExpectation
import de.interact.domain.expectations.derivation.spi.EventPublisher
import de.interact.domain.expectations.derivation.spi.Interactions
import de.interact.domain.expectations.derivation.spi.SystemInteractionExpectations
import de.interact.domain.expectations.derivation.spi.UnitTestBasedInteractionExpectations
import de.interact.domain.expectations.specification.events.SystemPropertyExpectationAddedEvent
import de.interact.domain.shared.EntityReference
import de.interact.domain.testtwin.api.event.UnitTestAddedEvent
import de.interact.domain.testtwin.spi.UnitTestAddedEventListener

class InteractionExpectationDerivationService(
    private val interactions: Interactions,
    private val unitTestBasedInteractionExpectations: UnitTestBasedInteractionExpectations,
    private val systemInteractionExpectations: SystemInteractionExpectations,
    private val eventPublisher: EventPublisher
): UnitTestAddedEventListener {
    override fun onUnitTestCaseAdded(event: UnitTestAddedEvent) {
        val interactions = interactions.findForTest(event.test.id)
        val interactionsExpectations = interactions.map { interaction ->
            InteractionExpectation.UnitTestBasedInteractionExpectation(
                event.test,
                interaction.stimulus.message,
                interaction.reactions.map { it.interfaceId }.toSet()
            )
        }
        val storedExpectations = unitTestBasedInteractionExpectations.save(interactionsExpectations)
        storedExpectations.forEach { newExpectation ->
            eventPublisher.publish(InteractionExpectationAddedEvent.UnitTestBasedInteractionExpectationAddedEvent(
                EntityReference(newExpectation.id, newExpectation.version)))
        }
    }

    fun handleNewSystemPropertyExpectation(event: SystemPropertyExpectationAddedEvent) {
        TODO()
    }
}