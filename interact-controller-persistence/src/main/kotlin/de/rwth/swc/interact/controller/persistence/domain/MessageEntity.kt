package de.rwth.swc.interact.controller.persistence.domain

import de.rwth.swc.interact.domain.*
import org.springframework.data.annotation.Version
import org.springframework.data.neo4j.core.schema.DynamicLabels
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship
import java.util.*

const val MESSAGE_NODE_LABEL = "Message"

@Node(MESSAGE_NODE_LABEL)
internal data class MessageEntity(
    @Id
    val id: UUID = UUID.randomUUID(),
    val payload: String,
    val isParameter: Boolean = false,
    @DynamicLabels
    val labels: MutableList<String> = mutableListOf(),
    @Relationship(type = "SENT_BY")
    val sentBy: OutgoingInterfaceEntity? = null,
    @Relationship(type = "RECEIVED_BY")
    val receivedBy: IncomingInterfaceEntity? = null,
    @Relationship(type = "COPY_OF")
    val copyOf: MessageEntity? = null,
    @Relationship(type = "REPLACES")
    val replaces: MessageEntity? = null
) {

    init {
        if (this.isParameter && !this.labels.contains(Label.PARAMETER.name)) {
            this.labels.add(Label.PARAMETER.name)
        }
    }

    @Relationship(type = "NEXT")
    var next: MessageEntity? = null

    @Version
    var neo4jVersion: Long = 0
        private set

    fun hasLabel(label: String): Boolean {
        return labels.contains(label)
    }

    enum class Label {
        PARAMETER,
        STIMULUS,
        COMPONENT_RESPONSE,
        ENVIRONMENT_RESPONSE
    }

    fun toDomain() = when {
        this.sentBy != null -> SentMessage(
                MessageType.Sent.COMPONENT_RESPONSE,
                MessageValue(this.payload),
                this.sentBy.toDomain()
            ).also {
                it.id = MessageId(this.id)
            }
        this.receivedBy != null -> ReceivedMessage(
                if(hasLabel(MessageType.Received.STIMULUS.name)) MessageType.Received.STIMULUS else MessageType.Received.ENVIRONMENT_RESPONSE,
                MessageValue(this.payload),
                this.receivedBy.toDomain(),
                this.isParameter
            ).also {
                it.id = MessageId(this.id)
            }
        else -> throw IllegalStateException("MessageEntity must have either a sentBy or receivedBy relationship")
    }
}

internal fun Message.toEntity() = when(this) {
    is SentMessage -> MessageEntity(
        this.id?.id ?: UUID.randomUUID(),
        this.value.value,
        false,
        mutableListOf(MessageEntity.Label.COMPONENT_RESPONSE.name),
        this.sentBy.toEntity(),
        null,
        null,
        null
    )
    is ReceivedMessage -> MessageEntity(
        this.id?.id ?: UUID.randomUUID(),
        this.value.value,
        this.isParameter,
        mutableListOf(
            if(this.messageType == MessageType.Received.STIMULUS) MessageEntity.Label.STIMULUS.name else MessageEntity.Label.ENVIRONMENT_RESPONSE.name
        ),
        null,
        this.receivedBy.toEntity(),
        null,
        null
    )
}

internal interface MessageEntityNoRelations{
    val id: UUID
    val payload: String
    val isParameter: Boolean
    val labels: MutableList<String>
}