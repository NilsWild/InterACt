package de.interact.domain.expectations.derivation.events

import de.interact.domain.shared.EntityReference
import de.interact.domain.shared.InteractionExpectationId
import de.interact.domain.shared.SystemInteractionExpectationId
import de.interact.domain.shared.UnitTestBasedInteractionExpectationId

sealed class InteractionExpectationAddedEvent: DerivationEvent {
    abstract val interactionExpectation: EntityReference<InteractionExpectationId>

    data class UnitTestBasedInteractionExpectationAddedEvent(
        override val interactionExpectation: EntityReference<UnitTestBasedInteractionExpectationId>
    ) : InteractionExpectationAddedEvent()

    data class SystemInteractionExpectationAddedEvent(
        override val interactionExpectation: EntityReference<SystemInteractionExpectationId>
    ) : InteractionExpectationAddedEvent()
}