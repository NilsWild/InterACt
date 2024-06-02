package de.interact.domain.testobservation.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import de.interact.domain.shared.Protocol
import de.interact.domain.shared.ProtocolData

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type")
@JsonSubTypes(
    JsonSubTypes.Type(IncomingInterface::class),
    JsonSubTypes.Type(OutgoingInterface::class)
)
sealed class ComponentInterface {
    abstract val protocol: Protocol
    abstract val protocolData: ProtocolData
}

data class IncomingInterface(
    override val protocol: Protocol,
    override val protocolData: ProtocolData,
) : ComponentInterface()

data class OutgoingInterface(
    override val protocol: Protocol,
    override val protocolData: ProtocolData
) : ComponentInterface()