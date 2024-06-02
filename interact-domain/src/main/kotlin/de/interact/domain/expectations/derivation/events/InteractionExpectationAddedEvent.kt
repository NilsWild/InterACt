package de.interact.domain.expectations.derivation.events

import de.interact.domain.shared.InteractionExpectationId
import de.interact.domain.shared.SystemInteractionExpectationId
import de.interact.domain.shared.UnitTestBasedInteractionExpectationId

sealed class InteractionExpectationAddedEvent: DerivationEvent {
    abstract val interactionExpectationId: InteractionExpectationId

    data class UnitTestBasedInteractionExpectationAddedEvent(
        override val interactionExpectationId: UnitTestBasedInteractionExpectationId
    ) : InteractionExpectationAddedEvent()

    data class SystemInteractionExpectationAddedEvent(
        override val interactionExpectationId: SystemInteractionExpectationId
    ) : InteractionExpectationAddedEvent()
}