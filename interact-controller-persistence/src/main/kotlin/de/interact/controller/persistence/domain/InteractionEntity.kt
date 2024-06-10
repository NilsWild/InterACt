package de.interact.controller.persistence.domain

import de.interact.domain.shared.EntityReference
import de.interact.domain.shared.EntityReferenceWithLabelsProjection
import de.interact.domain.shared.InteractionId
import org.springframework.data.neo4j.core.schema.DynamicLabels
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship

const val INTERACTION_NODE_LABEL = "Interaction"
const val PENDING_INTERACTION_NODE_LABEL = "PendingInteraction"
const val EXECUTABLE_INTERACTION_NODE_LABEL = "ExecutableInteraction"
const val FINISHED_INTERACTION_NODE_LABEL = "FinishedInteraction"
const val VALIDATED_INTERACTION_NODE_LABEL = "ValidatedInteraction"
const val FAILED_INTERACTION_NODE_LABEL = "FailedInteraction"

const val NEXT_INTERACTION_RELATIONSHIP_LABEL = "NEXT_INTERACTION"
const val DERIVED_FROM_RELATIONSHIP_LABEL = "DERIVED_FROM"
const val FROM_RELATIONSHIP_LABEL = "FROM"
const val TO_RELATIONSHIP_LABEL = "TO"
const val VALIDATED_BY_RELATIONSHIP_LABEL = "VALIDATED_BY"

@Node(INTERACTION_NODE_LABEL)
class InteractionEntity: Entity() {

    @Relationship(type = NEXT_INTERACTION_RELATIONSHIP_LABEL)
    lateinit var next: Set<InteractionEntity>

    @Relationship(type = NEXT_INTERACTION_RELATIONSHIP_LABEL, direction = Relationship.Direction.INCOMING)
    lateinit var previous: Set<InteractionEntity>

    @Relationship(type = DERIVED_FROM_RELATIONSHIP_LABEL)
    lateinit var derivedFrom: UnitTestEntity

    @Relationship(type = FROM_RELATIONSHIP_LABEL)
    lateinit var from: Set<IncomingInterfaceEntity>

    @Relationship(type = TO_RELATIONSHIP_LABEL)
    lateinit var to: Set<OutgoingInterfaceEntity>

    @Relationship(type = VALIDATED_BY_RELATIONSHIP_LABEL)
    lateinit var testCase: TestCaseEntity

    @DynamicLabels
    var labels: Set<String> = emptySet()

}

fun interactionEntityReference(id: InteractionId, version: Long?): InteractionEntity {
    return InteractionEntity().also {
        it.id = id.value
        it.version = version
    }
}

fun pendingInteractionEntity(id: InteractionId, version: Long?, previous: Set<InteractionEntity>, derivedFrom: UnitTestEntity, from: Set<IncomingInterfaceEntity>, to: Set<OutgoingInterfaceEntity>, testCase: TestCaseEntity): InteractionEntity {
    return interactionEntityReference(id, version).also {
        it.previous = previous
        it.derivedFrom = derivedFrom
        it.from = from
        it.to = to
        it.testCase = testCase
        it.labels = setOf(PENDING_INTERACTION_NODE_LABEL)
    }
}

fun executableInteractionEntity(id: InteractionId, version: Long?, previous: Set<InteractionEntity>, derivedFrom: UnitTestEntity, from: Set<IncomingInterfaceEntity>, to: Set<OutgoingInterfaceEntity>, testCase: TestCaseEntity): InteractionEntity {
    return interactionEntityReference(id, version).also {
        it.previous = previous
        it.derivedFrom = derivedFrom
        it.from = from
        it.to = to
        it.testCase = testCase
        it.labels = setOf(EXECUTABLE_INTERACTION_NODE_LABEL)
    }
}

fun validatedInteractionEntity(id: InteractionId, version: Long?, previous: Set<InteractionEntity>, derivedFrom: UnitTestEntity, from: Set<IncomingInterfaceEntity>, to: Set<OutgoingInterfaceEntity>, testCase: TestCaseEntity): InteractionEntity {
    return interactionEntityReference(id, version).also {
        it.previous = previous
        it.derivedFrom = derivedFrom
        it.from = from
        it.to = to
        it.testCase = testCase
        it.labels = setOf(VALIDATED_INTERACTION_NODE_LABEL)
    }
}

fun failedInteractionEntity(id: InteractionId, version: Long?, previous: Set<InteractionEntity>, derivedFrom: UnitTestEntity, from: Set<IncomingInterfaceEntity>, to: Set<OutgoingInterfaceEntity>, testCase: TestCaseEntity): InteractionEntity {
    return interactionEntityReference(id, version).also {
        it.previous = previous
        it.derivedFrom = derivedFrom
        it.from = from
        it.to = to
        it.testCase = testCase
        it.labels = setOf(FAILED_INTERACTION_NODE_LABEL)
    }
}

interface InteractionReferenceProjection: EntityReferenceWithLabelsProjection