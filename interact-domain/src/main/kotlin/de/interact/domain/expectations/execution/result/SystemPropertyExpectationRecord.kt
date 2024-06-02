package de.interact.domain.expectations.execution.result

import de.interact.domain.shared.SystemPropertyExpectationIdentifier
import de.interact.domain.shared.InterfaceSpec

data class SystemPropertyExpectationRecord(
    val identifier: SystemPropertyExpectationIdentifier,
    val fromInterface: InterfaceSpec,
    val toInterface: InterfaceSpec
)