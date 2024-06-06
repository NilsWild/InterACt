package de.interact.domain.rest

import de.interact.domain.shared.IncomingInterfaceExpectationId
import de.interact.domain.shared.InterfaceExpectationId
import de.interact.domain.shared.OutgoingInterfaceExpectationId

sealed interface RestInterfaceExpectation {
    val id: InterfaceExpectationId
    val protocolData: RestInterfaceData
}

data class IncomingRestInterfaceExpectation(
    override val id: IncomingInterfaceExpectationId,
    override val protocolData: RestInterfaceData
) : RestInterfaceExpectation

data class OutgoingRestInterfaceExpectation(
    override val id: OutgoingInterfaceExpectationId,
    override val protocolData: RestInterfaceData
) : RestInterfaceExpectation
