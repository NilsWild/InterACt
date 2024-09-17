package de.interact.domain.expectations.validation.spi

import de.interact.domain.expectations.validation.`interface`.Interface
import de.interact.domain.shared.OutgoingInterfaceId
import de.interact.domain.shared.ReceivedMessageId
import de.interact.domain.shared.SentMessageId

interface Interfaces {
    fun findIncomingInterfacesBoundToOutgoingInterface(outgoingInterfaceId: OutgoingInterfaceId): Set<Interface.IncomingInterface>
    fun findIncomingInterfaceMessageWasReceivedOn(receivedMessageId: ReceivedMessageId): Interface.IncomingInterface
    fun findOutgoingInterfaceMessageWasSentBy(sentMessageId: SentMessageId): Interface.OutgoingInterface
}