package de.interact.domain.expectations.specification.api

import de.interact.domain.expectations.execution.result.ExpectationsExecutionResult
import de.interact.domain.expectations.specification.collection.ExpectationsCollection
import de.interact.domain.expectations.specification.collection.addSystemPropertyExpectations
import de.interact.domain.expectations.specification.spi.EventPublisher
import de.interact.domain.expectations.specification.spi.ExpectationsCollections

class ExpectationsCollectionsManagementService(
    private val expectationsCollections: ExpectationsCollections,
    private val eventPublisher: EventPublisher
) {
    fun handle(expectationsExecutionResult: ExpectationsExecutionResult) {
        expectationsExecutionResult.systemPropertyExpectationsCollections.forEach {
            var collection = expectationsCollections.findByNameAndVersion(it.name, it.version)
            if (collection != null) {
                collection = expectationsCollections.save(collection.addSystemPropertyExpectations(it.expectations))
            } else {
                collection = ExpectationsCollection(it.name, it.version)
                collection = expectationsCollections.save(collection.addSystemPropertyExpectations(it.expectations))
            }
            collection.postPersistEvents.forEach(eventPublisher::publish)
        }
    }
}