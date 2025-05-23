package de.interact.domain.expectations.derivation.spi

import de.interact.domain.expectations.derivation.events.InteractionExpectationAddedEvent

interface UnitTestBasedInteractionExpectationAddedEventListener {
    fun onUnitTestBasedInteractionExpectationAdded(event: InteractionExpectationAddedEvent.UnitTestBasedInteractionExpectationAddedEvent)
}