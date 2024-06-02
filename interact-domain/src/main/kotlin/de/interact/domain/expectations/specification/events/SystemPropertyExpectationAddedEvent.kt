package de.interact.domain.expectations.specification.events

import de.interact.domain.shared.SystemPropertyExpectationId

data class SystemPropertyExpectationAddedEvent(
    val systemPropertyExpectationId: SystemPropertyExpectationId
): SpecificationEvent