package de.interact.domain.expectations.validation.spi

import de.interact.domain.expectations.validation.`interface`.Interface
import de.interact.domain.shared.OutgoingInterfaceId

interface Interfaces {
    fun findIncomingInterfacesBoundToOutgoingInterface(outgoingInterfaceId: OutgoingInterfaceId): Set<Interface.IncomingInterface>
}