package de.interact.domain.expectations.validation.`interface`

import de.interact.domain.shared.*

sealed class Interface: Entity<InterfaceId>(){

    data class IncomingInterface(
        override val id: IncomingInterfaceId,
        override val version: Long
    ) : Interface()

    data class OutgoingInterface(
        override val id: OutgoingInterfaceId,
        override val version: Long
    ) : Interface()
}

fun Interface.IncomingInterface.toEntityReference() = EntityReference(id, version)

fun Interface.OutgoingInterface.toEntityReference() = EntityReference(id, version)