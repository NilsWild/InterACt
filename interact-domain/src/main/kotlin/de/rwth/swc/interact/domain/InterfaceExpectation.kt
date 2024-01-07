package de.rwth.swc.interact.domain

import java.util.UUID

sealed interface InterfaceExpectation {
    val protocol: Protocol
    val protocolData: ProtocolData
    var id: InterfaceExpectationId?
}

data class IncomingInterfaceExpectation(
    override val protocol: Protocol,
    override val protocolData: ProtocolData,
) : InterfaceExpectation {
    override var id: InterfaceExpectationId? = null
}

data class OutgoingInterfaceExpectation(
    override val protocol: Protocol,
    override val protocolData: ProtocolData
) : InterfaceExpectation {
    override var id: InterfaceExpectationId? = null
}

@JvmInline
value class InterfaceExpectationId(val id: UUID) {
    override fun toString() = id.toString()

    companion object {
        fun random() = InterfaceExpectationId(UUID.randomUUID())
    }
}