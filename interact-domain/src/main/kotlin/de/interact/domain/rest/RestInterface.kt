package de.interact.domain.rest

import de.interact.domain.shared.IncomingInterfaceId
import de.interact.domain.shared.InterfaceId
import de.interact.domain.shared.OutgoingInterfaceId

sealed interface RestInterface {
    val id: InterfaceId
    val protocolData: RestInterfaceData
}

data class IncomingRestInterface(
    override val id: IncomingInterfaceId,
    override val protocolData: RestInterfaceData
) : RestInterface

data class OutgoingRestInterface(
    override val id: OutgoingInterfaceId,
    override val protocolData: RestInterfaceData
) : RestInterface