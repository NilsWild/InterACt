package de.interact.domain.specification.service

import arrow.core.getOrElse
import de.interact.domain.expectations.execution.result.ExpectationsExecutionResult
import de.interact.domain.expectations.execution.result.SystemPropertyExpectationRecord
import de.interact.domain.expectations.execution.result.SystemPropertyExpectationsCollectionReference
import de.interact.domain.expectations.execution.result.addSystemPropertyExpectation
import de.interact.domain.specification.spi.ExpectationsPublisher

class ExpectationsManager(
    private val expectationsPublisher: ExpectationsPublisher
) {
    private var expectationsExecutionResult = ExpectationsExecutionResult()

    fun publishExpectations() {
        expectationsPublisher.publish(expectationsExecutionResult)
    }

    fun addSystemProperty(
        collectionReference: SystemPropertyExpectationsCollectionReference,
        systemPropertyExpectationRecord: SystemPropertyExpectationRecord
    ) {
        expectationsExecutionResult = expectationsExecutionResult.addSystemPropertyExpectation(
            collectionReference,
            systemPropertyExpectationRecord
        ).getOrElse { error ->
            throw RuntimeException("Could not add system property expectation: $error")
        }
    }
}