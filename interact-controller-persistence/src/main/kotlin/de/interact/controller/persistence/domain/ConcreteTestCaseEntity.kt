package de.interact.controller.persistence.domain

import de.interact.domain.shared.*
import de.interact.domain.testtwin.abstracttest.concretetest.ConcreteTestCaseIdentifier
import de.interact.domain.testtwin.abstracttest.concretetest.TestParameter
import org.springframework.data.annotation.Transient
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship
import java.util.*

const val CONCRETE_TEST_CASE_NODE_LABEL = "ConcreteTestCase"
const val UNIT_TEST_NODE_LABEL = "UnitTest"
const val INTERACTION_TEST_NODE_LABEL = "InteractionTest"
const val TRIGGERED_MESSAGES_RELATIONSHIP_LABEL = "TRIGGERED_MESSAGES"

@Node(CONCRETE_TEST_CASE_NODE_LABEL)
sealed class ConcreteTestCaseEntity: Entity() {

    lateinit var identifier: String

    @Transient
    var labels: Set<String> = setOf(CONCRETE_TEST_CASE_NODE_LABEL)

    var parameters = listOf<String>()

    @Relationship(type = TRIGGERED_MESSAGES_RELATIONSHIP_LABEL)
    var triggeredMessages: SortedSet<MessageEntity> = sortedSetOf()

    @Relationship(type = TEMPLATE_FOR_RELATIONSHIP_LABEL, direction = Relationship.Direction.INCOMING)
    var template: AbstractTestCaseEntity? = null

    lateinit var status: String

}

@Node(UNIT_TEST_NODE_LABEL)
class UnitTestEntity : ConcreteTestCaseEntity() {

    init {
        labels += setOf(UNIT_TEST_NODE_LABEL)
    }

}

@Node(INTERACTION_TEST_NODE_LABEL)
class InteractionTestEntity : ConcreteTestCaseEntity() {

    init {
        labels += setOf(INTERACTION_TEST_NODE_LABEL)
    }
}

fun unitTestEntityReference(id: UnitTestId, version: Long?): UnitTestEntity {
    return UnitTestEntity().also {
        it.id = id.value
        it.version = version
    }
}

fun unitTestEntity(
    id: UnitTestId,
    version: Long? = null,
    identifier: ConcreteTestCaseIdentifier,
    parameters: List<TestParameter>,
    triggeredMessages: SortedSet<MessageEntity>,
    status: String): UnitTestEntity {
    return unitTestEntityReference(id,version).also {
        it.identifier = identifier.value
        it.parameters = parameters.map { it.value.toString() }
        it.triggeredMessages = triggeredMessages
        it.status = status
    }
}


fun interactionTestEntityReference(id: InteractionTestId, version: Long?): InteractionTestEntity {
    return InteractionTestEntity().also {
        it.id = id.value
        it.version = version
    }
}

fun interactionTestEntity(
    id: InteractionTestId,
    version: Long? = null,
    identifier: ConcreteTestCaseIdentifier,
    parameters: List<TestParameter>,
    triggeredMessages: SortedSet<MessageEntity>,
    status: String): InteractionTestEntity {
    return interactionTestEntityReference(id,version).also {
        it.identifier = identifier.value
        it.parameters = parameters.map { it.value.toString() }
        it.triggeredMessages = triggeredMessages
        it.status = status
    }
}

fun EntityReference<UnitTestId>.toEntity(): UnitTestEntity {
    return unitTestEntityReference(id, version)
}

fun EntityReference<InteractionTestId>.toEntity(): InteractionTestEntity {
    return interactionTestEntityReference(id, version)
}

fun EntityReference<TestId>.toEntity(): ConcreteTestCaseEntity {
    return when (id) {
        is UnitTestId -> unitTestEntityReference(id as UnitTestId, version)
        is InteractionTestId -> interactionTestEntityReference(id as InteractionTestId, version)
    }
}

interface ConcreteTestCaseReferenceProjection: EntityReferenceWithLabelsProjection

fun ConcreteTestCaseReferenceProjection.toEntityReference(): EntityReference<TestId> {
    return when {
        labels.contains(UNIT_TEST_NODE_LABEL) -> EntityReference(UnitTestId(id),version)
        labels.contains(INTERACTION_TEST_NODE_LABEL) -> EntityReference(InteractionTestId(id),version)
        else -> throw IllegalArgumentException("Unknown concrete test case type")
    }
}

interface UnitTestReferenceProjection: ConcreteTestCaseReferenceProjection

fun UnitTestReferenceProjection.toEntityReference(): EntityReference<UnitTestId> {
    return EntityReference(UnitTestId(id),version)
}