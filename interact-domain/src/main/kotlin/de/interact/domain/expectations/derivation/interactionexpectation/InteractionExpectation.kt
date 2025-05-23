package de.interact.domain.expectations.derivation.interactionexpectation

import de.interact.domain.shared.*
import java.util.*

sealed class InteractionExpectation: Entity<InteractionExpectationId>() {

    abstract val expectFrom: EntityReference<MessageId>
    abstract val expectTo: Set<EntityReference<InterfaceId>>
    abstract val requires: Set<EntityReference<InteractionExpectationId>>
    abstract val status: InteractionExpectationStatus

    data class UnitTestBasedInteractionExpectation(
        val derivedFrom: EntityReference<UnitTestId>,
        override val expectFrom: EntityReference<ComponentResponseMessageId>,
        override val expectTo: Set<EntityReference<IncomingInterfaceId>>,
        override val requires: Set<EntityReference<InteractionExpectationId>> = emptySet(),
        override val status: InteractionExpectationStatus = InteractionExpectationStatus.NoValidatingPathFound,
        override val id: UnitTestBasedInteractionExpectationId = UnitTestBasedInteractionExpectationId(UUID.randomUUID()),
        override val version: Long? = null
    ) : InteractionExpectation()

    data class SystemInteractionExpectation(
        val derivedFrom: EntityReference<SystemPropertyExpectationId>,
        override val expectFrom: EntityReference<MessageId>,
        override val expectTo: Set<EntityReference<InterfaceId>>,
        override val requires: Set<EntityReference<InteractionExpectationId>> = emptySet(),
        override val status: InteractionExpectationStatus = InteractionExpectationStatus.NoValidatingPathFound,
        override val id: SystemInteractionExpectationId = SystemInteractionExpectationId(UUID.randomUUID()),
        override val version: Long? = null
    ) : InteractionExpectation()

}

