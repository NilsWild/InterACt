package de.interact.domain.expectations.execution.result

import de.interact.domain.shared.SystemInteractionExpectationId

data class SystemExpectationExecutionResult(
    val id: SystemInteractionExpectationId,
    val status: Status
) {
    enum class Status {
        SUCCESS,
        FAILURE
    }
}