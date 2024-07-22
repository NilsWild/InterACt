package de.interact.domain.expectations.validation.`interface`

import de.interact.domain.shared.*

sealed class Interface<ID: InterfaceId>: Entity<ID>(){

    data class IncomingInterface(
        override val id: IncomingInterfaceId,
        override val version: Long
    ) : Interface<IncomingInterfaceId>()

    data class OutgoingInterface(
        override val id: OutgoingInterfaceId,
        override val version: Long
    ) : Interface<OutgoingInterfaceId>()
}