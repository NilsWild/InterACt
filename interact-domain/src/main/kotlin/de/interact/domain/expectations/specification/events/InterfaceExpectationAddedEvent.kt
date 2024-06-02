package de.interact.domain.expectations.specification.events

import de.interact.domain.shared.*

sealed class InterfaceExpectationAddedEvent: SpecificationEvent{
    abstract val id: InterfaceExpectationId
    abstract val protocol: Protocol
    abstract val protocolData: ProtocolData

    sealed class IncomingInterfaceExpectationAddedEvent: InterfaceExpectationAddedEvent() {
        abstract override val id: IncomingInterfaceExpectationId

        data class IndirectIncomingInterfaceExpectationAddedEvent(
            override val id: IndirectIncomingInterfaceExpectationId,
            override val protocol: Protocol,
            override val protocolData: ProtocolData
        ): IncomingInterfaceExpectationAddedEvent()

        data class DirectIncomingInterfaceExpectationAddedEvent(
            override val id: DirectIncomingInterfaceExpectationId,
            override val protocol: Protocol,
            override val protocolData: ProtocolData
        ): IncomingInterfaceExpectationAddedEvent()
    }

    sealed class OutgoingInterfaceExpectationAddedEvent: InterfaceExpectationAddedEvent() {
        abstract override val id: OutgoingInterfaceExpectationId

        data class DirectOutgoingInterfaceExpectationAddedEvent(
            override val id: DirectOutgoingInterfaceExpectationId,
            override val protocol: Protocol,
            override val protocolData: ProtocolData
        ): OutgoingInterfaceExpectationAddedEvent()

        data class IndirectOutgoingInterfaceExpectationAddedEvent(
            override val id: IndirectOutgoingInterfaceExpectationId,
            override val protocol: Protocol,
            override val protocolData: ProtocolData
        ): OutgoingInterfaceExpectationAddedEvent()
    }
}