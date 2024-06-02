package de.interact.domain.expectations.validation.plan

import de.interact.domain.shared.EntityReference
import de.interact.domain.shared.IncomingInterfaceId
import de.interact.domain.shared.ReceivedMessageId

data class MessageToReplaceIdentifier(
    val messageIdInOriginalUnitTest: EntityReference<ReceivedMessageId>,
    val interfaceId: EntityReference<IncomingInterfaceId>
)
