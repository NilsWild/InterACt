package de.rwth.swc.interact.controller.persistence.domain

import de.rwth.swc.interact.domain.*
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
    @DynamicLabels
    val labels: MutableList<String> = mutableListOf()
) {

    init {
        if (!this.labels.contains(this.mode.name + "Test")) {
            this.labels.add(this.mode.name + "Test")
        }
    }

    @Relationship(type = "TRIGGERED")
    var triggeredMessages: Set<MessageEntity> = emptySet()

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
        triggeredMessages.lastOrNull()?.next = it
        triggeredMessages = triggeredMessages.plusElement(it)
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
        triggeredMessages.lastOrNull()?.next = it
        triggeredMessages = triggeredMessages.plusElement(it)
    }

    fun toDomain() = ConcreteTestCase(
        ConcreteTestCaseName(name),
        mode,
        emptyList()
    ).also {
        it.id = ConcreteTestCaseId(id)
        it.result = result
        it.observedMessages = triggeredMessages.map { it.toDomain() }.toMutableList()
    }

}

internal fun ConcreteTestCase.toEntity() = ConcreteTestCaseEntity(
    this.id?.id ?: UUID.randomUUID(),
    this.name.name,
    this.result,
    this.mode
).also { entity ->
    entity.triggeredMessages = this.observedMessages.map { it.toEntity() }.toSet()
}

internal interface ConcreteTestCaseEntityNoRelations {
    val id: UUID
    val name: String
    val result: TestResult
    val mode: TestMode
    val labels: MutableList<String>
}