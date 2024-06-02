package de.interact.domain.expectations

import de.interact.domain.shared.InterfaceExpectationId

data class InterfaceExpectationUpdatedEvent(
    val interfaceExpectationId: InterfaceExpectationId
)
