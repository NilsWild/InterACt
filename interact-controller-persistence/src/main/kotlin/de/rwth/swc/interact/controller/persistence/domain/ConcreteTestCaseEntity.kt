package de.rwth.swc.interact.controller.persistence.domain

import de.rwth.swc.interact.domain.*
import de.rwth.swc.interact.domain.serialization.SerializationConstants
import org.springframework.data.annotation.Version
import org.springframework.data.neo4j.core.schema.DynamicLabels
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship
import java.util.*

const val CONCRETE_TEST_CASE_NODE_LABEL = "ConcreteTestCase"

@Node(CONCRETE_TEST_CASE_NODE_LABEL)
internal data class ConcreteTestCaseEntity(
    @Id
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val result: TestResult,
    val mode: TestMode,
    val parameters: String = "",
    @DynamicLabels
    val labels: MutableList<String> = mutableListOf()
) {

    init {
        if (!this.labels.contains(this.mode.name + "Test")) {
            this.labels.add(this.mode.name + "Test")
        }
    }

    @Relationship(type = "TRIGGERED")
    var triggeredMessages: SortedSet<MessageOrderRelationship> = sortedSetOf()

    @Version
    var neo4jVersion: Long = 0
        private set

    fun message(
        id: UUID = UUID.randomUUID(),
        payload: String,
        isParameter: Boolean,
        labels: MutableList<String> = mutableListOf(),
        sentBy: OutgoingInterfaceEntity,
        init: (MessageEntity.() -> Unit)? = null
    ) = MessageEntity(id, payload, isParameter, labels, sentBy, null).also {
        if (init != null) {
            it.init()
        }
        triggeredMessages.lastOrNull()?.message?.next = it
        triggeredMessages =
            triggeredMessages.plusElement(MessageOrderRelationship(triggeredMessages.size, it)).toSortedSet()
    }

    fun message(
        id: UUID = UUID.randomUUID(),
        payload: String,
        isParameter: Boolean,
        labels: MutableList<String> = mutableListOf(),
        receivedBy: IncomingInterfaceEntity,
        init: (MessageEntity.() -> Unit)? = null
    ) = MessageEntity(id, payload, isParameter, labels, null, receivedBy).also {
        if (init != null) {
            it.init()
        }
        triggeredMessages.lastOrNull()?.message?.next = it
        triggeredMessages =
            triggeredMessages.plusElement(MessageOrderRelationship(triggeredMessages.size, it)).toSortedSet()
    }

    fun toDomain() = ConcreteTestCase(
        ConcreteTestCaseName(name),
        mode,
        SerializationConstants.mapper.readerForListOf(TestCaseParameter::class.java).readValue(parameters)
    ).also { testCase ->
        testCase.id = ConcreteTestCaseId(id)
        testCase.result = result
        testCase.observedMessages = triggeredMessages.map { it.message.toDomain() }.toMutableList()
    }

}

internal fun ConcreteTestCase.toEntity() = ConcreteTestCaseEntity(
    this.id?.id ?: UUID.randomUUID(),
    this.name.name,
    this.result,
    this.mode,
    SerializationConstants.mapper.writeValueAsString(this.parameters)
).also { entity ->
    entity.triggeredMessages =
        this.observedMessages.mapIndexed { index, message -> MessageOrderRelationship(index, message.toEntity()) }
            .toSortedSet()
}

internal interface ConcreteTestCaseEntityNoRelations {
    val id: UUID
    val name: String
    val result: TestResult
    val mode: TestMode
    val parameters: List<String>
    val labels: MutableList<String>
}