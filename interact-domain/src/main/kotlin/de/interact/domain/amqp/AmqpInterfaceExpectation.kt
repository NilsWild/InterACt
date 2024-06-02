package de.interact.domain.amqp

import java.util.*

sealed interface AmqpInterfaceExpectation {
    val id: UUID
    val protocolData: AmqpProtocolData
}

data class IncomingAmqpInterfaceExpectation(
    override val id: UUID,
    override val protocolData: OutgoingAmqpProtocolData
) : AmqpInterface

data class OutgoingAmqpInterfaceExpectation(
    override val id: UUID,
    override val protocolData: IncomingAmqpProtocolData
) : AmqpInterface