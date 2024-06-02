package de.interact.domain.expectations.specification.spi

import de.interact.domain.expectations.specification.collection.ExpectationsCollection
import de.interact.domain.expectations.specification.collection.ExpectationsCollectionName
import de.interact.domain.expectations.specification.collection.ExpectationsCollectionVersion

interface ExpectationsCollections {
    fun save(expectationsCollection: ExpectationsCollection): ExpectationsCollection
    fun findByNameAndVersion(name: ExpectationsCollectionName, version: ExpectationsCollectionVersion): ExpectationsCollection?
}