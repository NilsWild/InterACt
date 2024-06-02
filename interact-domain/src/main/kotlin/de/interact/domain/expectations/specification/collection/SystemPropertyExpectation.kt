package de.interact.domain.expectations.specification.collection

import de.interact.domain.shared.Entity
import de.interact.domain.shared.SystemPropertyExpectationId
import de.interact.domain.shared.SystemPropertyExpectationIdentifier

data class SystemPropertyExpectation(
    val identifier: SystemPropertyExpectationIdentifier,
    val fromInterface: InterfaceExpectation.IncomingInterfaceExpectation,
    val toInterface: InterfaceExpectation.OutgoingInterfaceExpectation,
    override val id: SystemPropertyExpectationId,
    override val version: Long? = null
): Entity<SystemPropertyExpectationId>()

