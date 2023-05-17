package de.rwth.swc.interact.controller.persistence.domain

import org.springframework.data.annotation.Version
import org.springframework.data.neo4j.core.schema.DynamicLabels
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship
import java.util.*

@Node
data class ConcreteTestCase(
    @Id
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val result: TestResult,
    val source: DataSource,
    @DynamicLabels
    val labels: MutableList<String> = mutableListOf()
) {

    init {
        if (!this.labels.contains(this.source.name + "Test")) {
            this.labels.add(this.source.name + "Test")
        }
    }

    @Relationship(type = "TRIGGERED")
    var triggeredMessages: Set<Message> = emptySet()
        private set

    @Version
    var neo4jVersion: Long = 0
        private set

    fun message(
        id: UUID = UUID.randomUUID(),
        payload: String,
        isParameter: Boolean,
        labels: MutableList<String> = mutableListOf(),
        sentBy: OutgoingInterface,
        init: (Message.() -> Unit)? = null
    ) = Message(id, payload, isParameter, labels, sentBy, null).also {
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
        receivedBy: IncomingInterface,
        init: (Message.() -> Unit)? = null
    ) = Message(id, payload, isParameter, labels, null, receivedBy).also {
        if (init != null) {
            it.init()
        }
        triggeredMessages.lastOrNull()?.next = it
        triggeredMessages = triggeredMessages.plusElement(it)
    }

    enum class TestResult {
        FAILED, SUCCESS
    }

    enum class DataSource {
        UNIT, INTERACTION
    }
}