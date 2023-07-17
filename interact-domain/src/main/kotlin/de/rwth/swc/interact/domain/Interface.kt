package de.rwth.swc.interact.domain

import java.util.*

sealed interface ComponentInterface {
    val protocol: Protocol
    val protocolData: ProtocolData
    var id: InterfaceId?
}

data class IncomingInterface(
    override val protocol: Protocol,
    override val protocolData: ProtocolData,
    ) : ComponentInterface{
    override var id: InterfaceId? = null
}

@JvmInline
value class InterfaceId(val id: UUID) {
    override fun toString(): String {
        return id.toString()
    }

    companion object {
        fun random() = InterfaceId(UUID.randomUUID())
    }
}

data class OutgoingInterface(
    override val protocol: Protocol,
    override val protocolData: ProtocolData
    ): ComponentInterface {
    override var id: InterfaceId? = null
}