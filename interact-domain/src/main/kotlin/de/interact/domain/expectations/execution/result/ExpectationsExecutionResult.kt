package de.interact.domain.expectations.execution.result

import arrow.core.Either
import arrow.core.getOrElse
import de.interact.domain.expectations.execution.result.error.ExpectationsExecutionResultError

data class ExpectationsExecutionResult(
    val systemPropertyExpectationsCollections: Set<SystemPropertyExpectationsCollection> = emptySet(),
    val systemExpectationsResults: Set<SystemExpectationExecutionResult> = emptySet()
)

fun ExpectationsExecutionResult.addSystemPropertyExpectation(
    collectionReference: SystemPropertyExpectationsCollectionReference,
    expectation: SystemPropertyExpectationRecord
): Either<ExpectationsExecutionResultError, ExpectationsExecutionResult> {
    var added = false;
    var newCollection = systemPropertyExpectationsCollections.map { collection ->
        if (collectionReference.references(collection)) {
            added = true
            collection.addExpectation(expectation).getOrElse { error -> return Either.Left(error)}
        } else {
            collection
        }
    }.toSet()

    if (!added) {
        newCollection = systemPropertyExpectationsCollections.plus(
            SystemPropertyExpectationsCollection(
                collectionReference.name,
                collectionReference.version,
                setOf(expectation)
            )
        )
    }
    return Either.Right(this.copy(systemPropertyExpectationsCollections = newCollection))
}

fun ExpectationsExecutionResult.addSystemExpectationExecutionResult(result: SystemExpectationExecutionResult): Either<ExpectationsExecutionResultError, ExpectationsExecutionResult>{
    if(systemExpectationsResults.any { it.id == result.id }) {
        return Either.Left(ExpectationsExecutionResultError.MultipleResultsForOneExpectation(result.id))
    }
    return Either.Right(this.copy(systemExpectationsResults = systemExpectationsResults.plus(result)))
}

