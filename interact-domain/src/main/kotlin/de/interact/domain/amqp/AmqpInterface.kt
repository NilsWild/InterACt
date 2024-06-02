package de.interact.domain.amqp

import java.util.*

sealed interface AmqpInterface {
    val id: UUID
    val protocolData: AmqpProtocolData
}

data class IncomingAmqpInterface(
    override val id: UUID,
    override val protocolData: IncomingAmqpProtocolData
) : AmqpInterface

data class OutgoingAmqpInterface(
    override val id: UUID,
    override val protocolData: OutgoingAmqpProtocolData
) : AmqpInterface