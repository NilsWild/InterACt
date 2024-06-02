package de.interact.domain.expectations.execution.result.error

import de.interact.domain.shared.SystemInteractionExpectationId
import de.interact.domain.shared.SystemPropertyExpectationIdentifier

sealed class ExpectationsExecutionResultError {
    data class MultipleResultsForOneExpectation(val expectationId: SystemInteractionExpectationId) : ExpectationsExecutionResultError()
    data class DuplicateSystemPropertyExpectation(val identifier: SystemPropertyExpectationIdentifier) : ExpectationsExecutionResultError()
}