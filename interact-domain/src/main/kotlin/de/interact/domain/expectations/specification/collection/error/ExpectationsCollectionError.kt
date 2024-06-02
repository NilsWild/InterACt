package de.interact.domain.expectations.specification.collection.error

import de.interact.domain.shared.SystemPropertyExpectationIdentifier

sealed class ExpectationsCollectionError {
    data class DuplicateSystemPropertyExpectation(val identifier: SystemPropertyExpectationIdentifier) : ExpectationsCollectionError()
}