package de.interact.domain.expectations.execution.result

import arrow.core.Either
import de.interact.domain.expectations.specification.collection.ExpectationsCollectionName
import de.interact.domain.expectations.specification.collection.ExpectationsCollectionVersion
import de.interact.domain.expectations.execution.result.error.ExpectationsExecutionResultError

data class SystemPropertyExpectationsCollection(
    val name: ExpectationsCollectionName,
    val version: ExpectationsCollectionVersion,
    val expectations: Set<SystemPropertyExpectationRecord> = emptySet()
)

internal fun SystemPropertyExpectationsCollection.addExpectation(expectation: SystemPropertyExpectationRecord): Either<ExpectationsExecutionResultError, SystemPropertyExpectationsCollection> {
    if(expectations.any { it.identifier == expectation.identifier }) {
        return Either.Left(ExpectationsExecutionResultError.DuplicateSystemPropertyExpectation(expectation.identifier))
    }
    return Either.Right(this.copy(expectations = expectations.plus(expectation)))
}