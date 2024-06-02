package de.interact.controller.persistence.domain

import de.interact.domain.shared.EntityReferenceProjection
import de.interact.domain.shared.ExpectationsCollectionId
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship

const val EXPECTATIONS_COLLECTION_NODE_LABEL = "ExpectationsCollection"
const val CONTAINS_RELATIONSHIP_LABEL = "CONTAINS"

@Node(EXPECTATIONS_COLLECTION_NODE_LABEL)
class ExpectationsCollectionEntity: Entity() {

    lateinit var name: String
    lateinit var versionName: String

    @Relationship(CONTAINS_RELATIONSHIP_LABEL)
    var expectations: Set<SystemPropertyExpectationEntity> = emptySet()
}


fun expectationsCollectionEntityReference(
    id: ExpectationsCollectionId,
    version: Long?
): ExpectationsCollectionEntity {
    return ExpectationsCollectionEntity().also {
        it.id = id.value
        it.version = version
    }
}

fun expectationsCollectionEntity(
    id: ExpectationsCollectionId,
    version: Long? = null,
    name: String,
    versionName: String,
    expectations: Set<SystemPropertyExpectationEntity> = emptySet()
): ExpectationsCollectionEntity {
    return expectationsCollectionEntityReference(id,version).also {
        it.name = name
        it.versionName = versionName
        it.expectations = expectations
    }
}


interface ExpectationsCollectionReferenceProjection: EntityReferenceProjection