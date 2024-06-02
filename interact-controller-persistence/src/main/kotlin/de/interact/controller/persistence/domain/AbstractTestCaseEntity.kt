package de.interact.controller.persistence.domain

import de.interact.domain.shared.AbstractTestId
import de.interact.domain.shared.EntityReference
import de.interact.domain.shared.EntityReferenceProjection
import de.interact.domain.testtwin.abstracttest.AbstractTestCaseIdentifier
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship

const val ABSTRACT_TEST_CASE_NODE_LABEL = "AbstractTestCase"
const val TEMPLATE_FOR_RELATIONSHIP_LABEL = "TEMPLATE_FOR"

@Node(ABSTRACT_TEST_CASE_NODE_LABEL)
class AbstractTestCaseEntity: Entity() {

    lateinit var identifier: String

    @Relationship(type = TEMPLATE_FOR_RELATIONSHIP_LABEL)
    var templateFor: Set<ConcreteTestCaseEntity> = setOf()

    @Relationship(type = TESTED_BY_RELATIONSHIP_LABEL, direction = Relationship.Direction.INCOMING)
    var test: VersionEntity? = null
}

fun abstractTestCaseEntityReference(id: AbstractTestId, version: Long?): AbstractTestCaseEntity{
    return AbstractTestCaseEntity().also {
        it.id = id.value
        it.version = version
    }
}

fun abstractTestCaseEntity(id: AbstractTestId, identifier: AbstractTestCaseIdentifier, templateFor: Set<ConcreteTestCaseEntity>): AbstractTestCaseEntity{
    return AbstractTestCaseEntity().also {
        it.id = id.value
        it.identifier = identifier.value
        it.templateFor = templateFor
    }
}

interface AbstractTestCaseReferenceProjection : EntityReferenceProjection

fun EntityReference<AbstractTestId>.toEntity(): AbstractTestCaseEntity {
    return abstractTestCaseEntityReference(id, version)
}

fun AbstractTestCaseReferenceProjection.toEntityReference(): EntityReference<AbstractTestId> {
    return EntityReference(AbstractTestId(id), version)
}
