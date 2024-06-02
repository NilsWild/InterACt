package de.interact.domain.expectations.validation.interactionexpectation

import de.interact.domain.shared.*
import java.util.*

sealed class InteractionExpectation: Entity<InteractionExpectationId>() {

    abstract val expectFrom: EntityReference<MessageId>
    abstract val expectTo: Set<EntityReference<InterfaceId>>
    abstract val requires: Set<EntityReference<InteractionExpectationId>>
    abstract val validationPlans: Set<EntityReference<ValidationPlanId>>

    data class UnitTestBasedInteractionExpectation(
        val derivedFrom: EntityReference<UnitTestId>,
        override val expectFrom: EntityReference<MessageId>,
        override val expectTo: Set<EntityReference<InterfaceId>>,
        override val requires: Set<EntityReference<InteractionExpectationId>> = emptySet(),
        override val validationPlans: Set<EntityReference<ValidationPlanId>> = emptySet(),
        override val id: UnitTestBasedInteractionExpectationId = UnitTestBasedInteractionExpectationId(UUID.randomUUID()),
        override val version: Long? = null
    ) : InteractionExpectation()

    data class SystemInteractionExpectation(
        override val expectFrom: EntityReference<MessageId>,
        override val expectTo: Set<EntityReference<InterfaceId>>,
        override val requires: Set<EntityReference<InteractionExpectationId>> = emptySet(),
        override val validationPlans: Set<EntityReference<ValidationPlanId>> = emptySet(),
        override val id: SystemInteractionExpectationId = SystemInteractionExpectationId(UUID.randomUUID()),
        override val version: Long? = null
    ) : InteractionExpectation()

}