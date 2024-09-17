package de.interact.domain.expectations.validation.plan

import de.interact.domain.shared.EntityReference
import de.interact.domain.shared.OutgoingInterfaceId
import de.interact.domain.shared.ReceivedMessageId
import de.interact.domain.shared.SentMessageId

data class ReplacementIdentifier(
    val messageInOriginalUnitTest: EntityReference<SentMessageId>,
    val interfaceToCopyFrom: EntityReference<OutgoingInterfaceId>
)