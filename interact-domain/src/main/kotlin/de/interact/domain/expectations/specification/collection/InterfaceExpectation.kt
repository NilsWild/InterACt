package de.interact.domain.expectations.specification.collection

import de.interact.domain.shared.*
import java.util.*

sealed class InterfaceExpectation: Entity<InterfaceExpectationId>() {
    abstract val protocol: Protocol
    abstract val protocolData: ProtocolData

    sealed class IncomingInterfaceExpectation: InterfaceExpectation() {
        data class IndirectIncomingInterfaceExpectation(
            override val protocol: Protocol,
            override val protocolData: ProtocolData,
            override val id: IndirectIncomingInterfaceExpectationId = IndirectIncomingInterfaceExpectationId(UUID.randomUUID()),
            override val version: Long? = null
        ) : IncomingInterfaceExpectation()

        data class DirectIncomingInterfaceExpectation(
            override val protocol: Protocol,
            override val protocolData: ProtocolData,
            override val id: DirectIncomingInterfaceExpectationId = DirectIncomingInterfaceExpectationId(UUID.randomUUID()),
            override val version: Long? = null
        ) : IncomingInterfaceExpectation()
    }

    sealed class OutgoingInterfaceExpectation: InterfaceExpectation() {
        data class IndirectOutgoingInterfaceExpectation(
            override val protocol: Protocol,
            override val protocolData: ProtocolData,
            override val id: IndirectOutgoingInterfaceExpectationId = IndirectOutgoingInterfaceExpectationId(UUID.randomUUID()),
            override val version: Long? = null
        ) : OutgoingInterfaceExpectation()

        data class DirectOutgoingInterfaceExpectation(
            override val protocol: Protocol,
            override val protocolData: ProtocolData,
            override val id: DirectOutgoingInterfaceExpectationId = DirectOutgoingInterfaceExpectationId(UUID.randomUUID()),
            override val version: Long? = null
        ) : OutgoingInterfaceExpectation()
    }
}