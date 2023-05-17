package de.rwth.swc.interact.controller.persistence.domain

import org.springframework.data.annotation.Version
import org.springframework.data.neo4j.core.schema.DynamicLabels
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship
import java.util.*

@Node
data class Message(
    @Id
    val id: UUID = UUID.randomUUID(),
    val payload: String,
    val isParameter: Boolean = false,
    @DynamicLabels
    val labels: MutableList<String> = mutableListOf(),
    @Relationship(type = "SENT_BY")
    val sentBy: OutgoingInterface? = null,
    @Relationship(type = "RECEIVED_BY")
    val receivedBy: IncomingInterface? = null,
    @Relationship(type = "COPY_OF")
    val copyOf: Message? = null,
    @Relationship(type = "REPLACES")
    val replaces: Message? = null
) {

    init {
        if (this.isParameter && !this.labels.contains(Label.PARAMETER.name)) {
            this.labels.add(Label.PARAMETER.name)
        }
    }

    @Relationship(type = "NEXT")
    var next: Message? = null

    @Version
    var neo4jVersion: Long = 0
        private set

    fun hasLabel(label: Label): Boolean {
        return labels.contains(label.name)
    }

    enum class Label {
        PARAMETER,
        STIMULUS,
        COMPONENT_RESPONSE,
        ENVIRONMENT_RESPONSE
    }
}