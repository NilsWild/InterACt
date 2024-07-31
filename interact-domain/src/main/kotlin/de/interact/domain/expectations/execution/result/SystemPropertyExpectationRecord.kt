package de.interact.domain.expectations.execution.result

import de.interact.domain.shared.InterfaceSpec
import de.interact.domain.shared.SystemPropertyExpectationIdentifier

data class SystemPropertyExpectationRecord(
    val identifier: SystemPropertyExpectationIdentifier,
    val fromInterface: InterfaceSpec,
    val toInterface: InterfaceSpec
)