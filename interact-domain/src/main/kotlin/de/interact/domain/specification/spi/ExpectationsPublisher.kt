package de.interact.domain.specification.spi

import de.interact.domain.expectations.execution.result.ExpectationsExecutionResult

interface ExpectationsPublisher {
    fun publish(result: ExpectationsExecutionResult): Boolean
}