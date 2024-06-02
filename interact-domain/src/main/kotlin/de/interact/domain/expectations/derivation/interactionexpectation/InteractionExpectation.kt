package de.interact.domain.expectations.derivation.interactionexpectation

import com.fasterxml.uuid.Generators
import de.interact.domain.shared.*

sealed class InteractionExpectation: Entity<InteractionExpectationId>() {

    abstract val expectFrom: EntityReference<MessageId>
    abstract val expectTo: Set<EntityReference<InterfaceId>>
    abstract val requires: Set<EntityReference<InteractionExpectationId>>

    data class UnitTestBasedInteractionExpectation(
        val derivedFrom: EntityReference<UnitTestId>,
        override val expectFrom: EntityReference<ComponentResponseMessageId>,
        override val expectTo: Set<EntityReference<IncomingInterfaceId>>,
        override val requires: Set<EntityReference<InteractionExpectationId>> = emptySet(),
        override val version: Long? = null
    ) : InteractionExpectation() {
        override val id: UnitTestBasedInteractionExpectationId = UnitTestBasedInteractionExpectationId(Generators.nameBasedGenerator().generate(
            hashedSha256(derivedFrom, expectFrom, expectTo, requires)
        ))
    }

    data class SystemInteractionExpectation(
        val derivedFrom: EntityReference<SystemPropertyExpectationId>,
        override val expectFrom: EntityReference<MessageId>,
        override val expectTo: Set<EntityReference<InterfaceId>>,
        override val requires: Set<EntityReference<InteractionExpectationId>> = emptySet(),
        override val version: Long? = null
    ) : InteractionExpectation() {
        override val id: SystemInteractionExpectationId = SystemInteractionExpectationId(Generators.nameBasedGenerator().generate(
            hashedSha256(derivedFrom, expectFrom, expectTo, requires)
        ))
    }

}

