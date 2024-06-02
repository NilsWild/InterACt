package de.interact.controller.persistence.domain

import de.interact.domain.shared.*
import org.springframework.data.annotation.Transient
import org.springframework.data.annotation.Version
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship
import java.util.*

const val INTERACTION_EXPECTATION_NODE_LABEL = "InteractionExpectation"
const val UNIT_TEST_BASED_INTERACTION_EXPECTATION_NODE_LABEL = "UnitTestBasedInteractionExpectation"
const val SYSTEM_INTERACTION_EXPECTATION_NODE_LABEL = "SystemInteractionExpectation"
const val EXPECT_FROM_RELATIONSHIP_LABEL = "EXPECT_FROM"
const val EXPECT_TO_RELATIONSHIP_LABEL = "EXPECT_TO"
const val REQUIRES_RELATIONSHIP_LABEL = "REQUIRES"

@Node(INTERACTION_EXPECTATION_NODE_LABEL)
sealed class InteractionExpectationEntity: Entity() {

    @Relationship(type = EXPECT_FROM_RELATIONSHIP_LABEL)
    lateinit var expectFrom: MessageEntity

    @Relationship(type = EXPECT_TO_RELATIONSHIP_LABEL)
    lateinit var expectTo: Set<InterfaceEntity>

    @Relationship(type = REQUIRES_RELATIONSHIP_LABEL)
    lateinit var requires: Set<InteractionExpectationEntity>

    @Relationship(CANDIDATE_FOR_RELATIONSHIP_LABEL, direction = Relationship.Direction.INCOMING)
    lateinit var validationPlans: Set<InteractionExpectationValidationPlanEntity>

    lateinit var status: String

    @Transient
    var labels: Set<String> = setOf(INTERACTION_EXPECTATION_NODE_LABEL)

}

@Node(UNIT_TEST_BASED_INTERACTION_EXPECTATION_NODE_LABEL)
class UnitTestBasedInteractionExpectationEntity : InteractionExpectationEntity() {

    init {
        labels += setOf(UNIT_TEST_BASED_INTERACTION_EXPECTATION_NODE_LABEL)
    }

    @Relationship(type = "DERIVED_FROM")
    lateinit var derivedFrom: UnitTestEntity
}

@Node(SYSTEM_INTERACTION_EXPECTATION_NODE_LABEL)
class SystemInteractionExpectationEntity : InteractionExpectationEntity() {

    init {
        labels += setOf(SYSTEM_INTERACTION_EXPECTATION_NODE_LABEL)
    }

    @Relationship(type = "DERIVED_FROM")
    lateinit var derivedFrom: SystemPropertyExpectationEntity
}

fun systemInteractionExpectationEntityReference(
    id: SystemInteractionExpectationId,
    version: Long?
): SystemInteractionExpectationEntity {
    return SystemInteractionExpectationEntity().also {
        it.id = id.value
        it.version = version
    }
}

fun systemInteractionExpectationEntity(
    id: SystemInteractionExpectationId,
    version: Long? = null,
    expectFrom: MessageEntity,
    expectTo: Set<InterfaceEntity>,
    requires: Set<InteractionExpectationEntity>,
    derivedFrom: SystemPropertyExpectationEntity
): SystemInteractionExpectationEntity {
    return systemInteractionExpectationEntityReference(id,version).also {
        it.expectFrom = expectFrom
        it.expectTo = expectTo
        it.requires = requires
        it.derivedFrom = derivedFrom
    }
}

fun unitTestBasedInteractionExpectationEntityReference(
    id: UnitTestBasedInteractionExpectationId,
    version: Long?
): UnitTestBasedInteractionExpectationEntity {
    return UnitTestBasedInteractionExpectationEntity().also {
        it.id = id.value
        it.version = version
    }
}

fun unitTestBasedInteractionExpectationEntity(
    id: UnitTestBasedInteractionExpectationId,
    version: Long? = null,
    expectFrom: MessageEntity,
    expectTo: Set<InterfaceEntity>,
    requires: Set<InteractionExpectationEntity>,
    derivedFrom: UnitTestEntity
): UnitTestBasedInteractionExpectationEntity {
    return unitTestBasedInteractionExpectationEntityReference(id,version).also {
        it.expectFrom = expectFrom
        it.expectTo = expectTo
        it.requires = requires
        it.derivedFrom = derivedFrom
    }
}

fun EntityReference<InteractionExpectationId>.toEntity(): InteractionExpectationEntity {
    return when (id) {
        is SystemInteractionExpectationId -> systemInteractionExpectationEntityReference(id as SystemInteractionExpectationId,version)
        is UnitTestBasedInteractionExpectationId -> unitTestBasedInteractionExpectationEntityReference(id as UnitTestBasedInteractionExpectationId,version)
    }
}


interface InteractionExpectationReferenceProjection: EntityReferenceWithLabelsProjection

interface UnitTestBasedInteractionExpectationReferenceProjection: InteractionExpectationReferenceProjection

interface SystemInteractionExpectationReferenceProjection: InteractionExpectationReferenceProjection

fun InteractionExpectationReferenceProjection.toEntityReference(): EntityReference<InteractionExpectationId> {
    return when {
        labels.contains(UNIT_TEST_BASED_INTERACTION_EXPECTATION_NODE_LABEL) -> EntityReference(UnitTestBasedInteractionExpectationId(id),version)
        labels.contains(SYSTEM_INTERACTION_EXPECTATION_NODE_LABEL) -> EntityReference(SystemInteractionExpectationId(id),version)
        else -> throw IllegalArgumentException("Unknown InteractionExpectationReference")
    }
}