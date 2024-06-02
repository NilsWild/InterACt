package de.interact.domain.testtwin.`interface`

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import de.interact.domain.shared.*

@JsonIdentityInfo(
    generator = ObjectIdGenerators.UUIDGenerator::class,
    property = "@id"
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type")
sealed class Interface: Entity<InterfaceId>() {
    abstract val protocol: Protocol
    abstract val protocolData: ProtocolData
}

data class IncomingInterface(
    override val id: IncomingInterfaceId,
    override val protocol: Protocol,
    override val protocolData: ProtocolData,
    override val version: Long? = null
) : Interface()

data class OutgoingInterface(
    override val id: OutgoingInterfaceId,
    override val protocol: Protocol,
    override val protocolData: ProtocolData,
    override val version: Long? = null
) : Interface()