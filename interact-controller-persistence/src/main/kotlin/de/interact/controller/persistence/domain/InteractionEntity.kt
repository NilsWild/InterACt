package de.interact.controller.persistence.domain

import de.interact.domain.shared.EntityReferenceWithLabelsProjection
import de.interact.domain.shared.InteractionId
import de.interact.domain.shared.ReceivedMessageId
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
class InteractionEntity: Entity(), Comparable<InteractionEntity> {

    @Relationship(type = NEXT_INTERACTION_RELATIONSHIP_LABEL)
    lateinit var next: Set<InteractionEntity>

    @Relationship(type = NEXT_INTERACTION_RELATIONSHIP_LABEL, direction = Relationship.Direction.INCOMING)
    lateinit var previous: Set<InteractionEntity>

    @Relationship(type = DERIVED_FROM_RELATIONSHIP_LABEL)
    lateinit var derivedFrom: UnitTestEntity

    @Relationship(type = FROM_RELATIONSHIP_LABEL)
    lateinit var from: Set<ReceivedMessageEntity>

    @Relationship(type = TO_RELATIONSHIP_LABEL)
    lateinit var to: Set<SentMessageEntity>

    @Relationship(type = VALIDATED_BY_RELATIONSHIP_LABEL)
    lateinit var testCase: TestCaseEntity

    var order: Int = 0

    @DynamicLabels
    var labels: Set<String> = emptySet()

    override fun compareTo(other: InteractionEntity): Int {
        return order - other.order
    }

}

fun interactionEntityReference(id: InteractionId, order: Int, version: Long?): InteractionEntity {
    return InteractionEntity().also {
        it.id = id.value
        it.order = order
        it.version = version
    }
}

fun pendingInteractionEntity(id: InteractionId, order:Int, version: Long?, previous: Set<InteractionEntity>, derivedFrom: UnitTestEntity, from: Set<ReceivedMessageEntity>, to: Set<SentMessageEntity>, testCase: TestCaseEntity): InteractionEntity {
    return interactionEntityReference(id, order, version).also {
        it.previous = previous
        it.derivedFrom = derivedFrom
        it.from = from
        it.to = to
        it.testCase = testCase
        it.labels = setOf(PENDING_INTERACTION_NODE_LABEL)
    }
}

fun executableInteractionEntity(id: InteractionId, order:Int, version: Long?, previous: Set<InteractionEntity>, derivedFrom: UnitTestEntity, from: Set<ReceivedMessageEntity>, to: Set<SentMessageEntity>, testCase: TestCaseEntity): InteractionEntity {
    return interactionEntityReference(id, order, version).also {
        it.previous = previous
        it.derivedFrom = derivedFrom
        it.from = from
        it.to = to
        it.testCase = testCase
        it.labels = setOf(EXECUTABLE_INTERACTION_NODE_LABEL)
    }
}

fun validatedInteractionEntity(id: InteractionId, order: Int, version: Long?, previous: Set<InteractionEntity>, derivedFrom: UnitTestEntity, from: Set<ReceivedMessageEntity>, to: Set<SentMessageEntity>, testCase: TestCaseEntity): InteractionEntity {
    return interactionEntityReference(id, order, version).also {
        it.previous = previous
        it.derivedFrom = derivedFrom
        it.from = from
        it.to = to
        it.testCase = testCase
        it.labels = setOf(VALIDATED_INTERACTION_NODE_LABEL)
    }
}

fun failedInteractionEntity(id: InteractionId, order: Int, version: Long?, previous: Set<InteractionEntity>, derivedFrom: UnitTestEntity, from: Set<ReceivedMessageEntity>, to: Set<SentMessageEntity>, testCase: TestCaseEntity): InteractionEntity {
    return interactionEntityReference(id, order, version).also {
        it.previous = previous
        it.derivedFrom = derivedFrom
        it.from = from
        it.to = to
        it.testCase = testCase
        it.labels = setOf(FAILED_INTERACTION_NODE_LABEL)
    }
}

interface InteractionReferenceProjection: EntityReferenceWithLabelsProjection