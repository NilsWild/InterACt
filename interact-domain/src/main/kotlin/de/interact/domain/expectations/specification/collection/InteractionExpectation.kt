package de.interact.domain.expectations.specification.collection

import de.interact.domain.shared.InterfaceId
import de.interact.domain.shared.MessageId
import de.interact.domain.shared.UnitTestId

sealed class InteractionExpectation {

    abstract val expectFrom: MessageId
    abstract val expectTo: List<InterfaceId>
    abstract val requires: Set<InteractionExpectation>
    abstract val leadsTo: List<MessageId>

    data class UnitTestBasedInteractionExpectation(
        val derivedFrom: UnitTestId,
        override val expectFrom: MessageId,
        override val expectTo: List<InterfaceId>,
        override val requires: Set<InteractionExpectation> = emptySet(),
        override val leadsTo: List<MessageId> = emptyList()
    ) : InteractionExpectation()

    data class SystemInteractionExpectation(
        override val expectFrom: MessageId,
        override val expectTo: List<InterfaceId>,
        override val requires: Set<InteractionExpectation> = emptySet(),
        override val leadsTo: List<MessageId> = emptyList()
    ) : InteractionExpectation()

}

