package de.interact.domain.testtwin.api.event

import de.interact.domain.shared.IncomingInterfaceId
import de.interact.domain.shared.InterfaceId
import de.interact.domain.shared.OutgoingInterfaceId

sealed interface InterfaceAddedToVersionEvent {
    val interfaceId: InterfaceId
    val protocol: String
}

data class IncomingInterfaceAddedToVersionEvent(
    override val interfaceId: IncomingInterfaceId,
    override val protocol: String
) : InterfaceAddedToVersionEvent

data class OutgoingInterfaceAddedToVersionEvent(
    override val interfaceId: OutgoingInterfaceId,
    override val protocol: String
) : InterfaceAddedToVersionEvent