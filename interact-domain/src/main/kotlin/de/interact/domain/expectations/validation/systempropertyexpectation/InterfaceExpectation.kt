package de.interact.domain.expectations.validation.systempropertyexpectation

import de.interact.domain.shared.InterfaceId
import de.interact.domain.shared.Protocol
import de.interact.domain.shared.ProtocolData

sealed class InterfaceExpectation {
    abstract val protocol: Protocol
    abstract val protocolData: ProtocolData
    abstract val matches: Set<InterfaceId>

    data class IncomingInterfaceExpectation(
        override val protocol: Protocol,
        override val protocolData: ProtocolData,
        override val matches: Set<InterfaceId> = emptySet()
    ) : InterfaceExpectation()

    data class OutgoingInterfaceExpectation(
        override val protocol: Protocol,
        override val protocolData: ProtocolData,
        override val matches: Set<InterfaceId> = emptySet()
    ) : InterfaceExpectation()
}