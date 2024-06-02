package de.interact.domain.testtwin.abstracttest.concretetest.message

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import de.interact.domain.shared.*
import de.interact.domain.testtwin.`interface`.IncomingInterface
import de.interact.domain.testtwin.`interface`.OutgoingInterface

@JsonIdentityInfo(
    generator = ObjectIdGenerators.UUIDGenerator::class,
    property = "@id"
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type")
sealed class Message : Comparable<Message> {
    abstract val id: MessageId
    abstract val value: MessageValue
    abstract val order: Int
    abstract val version: Long?

    sealed class SentMessage : Message() {
        abstract override val id: SentMessageId
        abstract val sentBy: OutgoingInterface
        abstract val dependsOn: Collection<ReceivedMessage>
    }

    sealed class ReceivedMessage : Message() {
        abstract override val id: ReceivedMessageId
        abstract val receivedBy: IncomingInterface
    }

    override fun compareTo(other: Message): Int {
        return order - other.order
    }
}

data class StimulusMessage(
    override val id: StimulusMessageId,
    override val value: MessageValue,
    override val receivedBy: IncomingInterface,
    override val version: Long? = null
) : Message.ReceivedMessage() {
    override val order = 0
}

data class ComponentResponseMessage(
    override val id: ComponentResponseMessageId,
    override val value: MessageValue,
    override val order: Int,
    override val sentBy: OutgoingInterface,
    override val dependsOn: Collection<ReceivedMessage>,
    override val version: Long? = null
) : Message.SentMessage()

data class EnvironmentResponseMessage(
    override val id: EnvironmentResponseMessageId,
    override val value: MessageValue,
    override val order: Int,
    override val receivedBy: IncomingInterface,
    val reactionTo: ComponentResponseMessage,
    override val version: Long? = null
) : Message.ReceivedMessage()

@JvmInline
value class MessageValue(val value: String) {
    override fun toString(): String {
        return value
    }
}