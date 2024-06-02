package de.interact.domain.expectations.execution.result

import de.interact.domain.expectations.specification.collection.ExpectationsCollectionName
import de.interact.domain.expectations.specification.collection.ExpectationsCollectionVersion

data class SystemPropertyExpectationsCollectionReference(
    val name: ExpectationsCollectionName,
    val version: ExpectationsCollectionVersion
) {
    infix fun references(collection: SystemPropertyExpectationsCollection): Boolean {
        return collection.name == name && collection.version == version
    }
}