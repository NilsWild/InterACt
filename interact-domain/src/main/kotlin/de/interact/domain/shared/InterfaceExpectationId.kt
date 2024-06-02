package de.interact.domain.shared

import java.util.*

sealed interface InterfaceExpectationId{
    val id: UUID
}

sealed interface IncomingInterfaceExpectationId: InterfaceExpectationId

sealed interface OutgoingInterfaceExpectationId: InterfaceExpectationId

@JvmInline
value class DirectIncomingInterfaceExpectationId(override val id: UUID): IncomingInterfaceExpectationId{
    override fun toString() = id.toString()
}

@JvmInline
value class IndirectIncomingInterfaceExpectationId(override val id: UUID): IncomingInterfaceExpectationId{
    override fun toString() = id.toString()
}

@JvmInline
value class DirectOutgoingInterfaceExpectationId(override val id: UUID): OutgoingInterfaceExpectationId{
    override fun toString() = id.toString()
}

@JvmInline
value class IndirectOutgoingInterfaceExpectationId(override val id: UUID): OutgoingInterfaceExpectationId{
    override fun toString() = id.toString()
}