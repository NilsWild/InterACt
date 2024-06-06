package de.interact.domain.testobservation.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type")
@JsonSubTypes(
    JsonSubTypes.Type(StimulusMessage::class),
    JsonSubTypes.Type(ComponentResponseMessage::class),
    JsonSubTypes.Type(EnvironmentResponseMessage::class)
)
sealed class Message : Comparable<Message> {
    abstract val value: MessageValue
    abstract val triggeredBy: ConcreteTestCase
    abstract val previous: Message?
    abstract val order: Int

    fun getPreviousReceivedMessages(): List<ReceivedMessage> {
        return when (previous) {
            is ReceivedMessage -> listOf(previous as ReceivedMessage) + (previous as ReceivedMessage).getPreviousReceivedMessages()
            is SentMessage -> (previous as SentMessage).getPreviousReceivedMessages()
            null -> emptyList()
        }
    }

    sealed class SentMessage : Message() {
        abstract val sentBy: OutgoingInterface
        abstract val dependsOn: Collection<ReceivedMessage>
    }

    sealed class ReceivedMessage : Message() {
        abstract val receivedBy: IncomingInterface
    }

    override fun compareTo(other: Message): Int {
        return order - other.order
    }
}

data class StimulusMessage(
    override val value: MessageValue,
    override val triggeredBy: ConcreteTestCase,
    override val receivedBy: IncomingInterface
) : Message.ReceivedMessage() {
    override val previous: Message? = null
    override val order = 0
}

data class ComponentResponseMessage(
    override val value: MessageValue,
    override val triggeredBy: ConcreteTestCase,
    override val previous: Message?,
    override val sentBy: OutgoingInterface
) : Message.SentMessage() {
    override val dependsOn: Collection<ReceivedMessage> = getPreviousReceivedMessages()
    override val order = previous?.order?.plus(1) ?: 0
}

data class EnvironmentResponseMessage(
    override val value: MessageValue,
    override val triggeredBy: ConcreteTestCase,
    override val previous: Message,
    override val receivedBy: IncomingInterface
) : Message.ReceivedMessage() {
    val reactionTo: ComponentResponseMessage = getPreviousComponentResponse()
    override val order = previous.order + 1

    fun getPreviousComponentResponse(): ComponentResponseMessage {
        return when (previous) {
            is ComponentResponseMessage -> previous
            is EnvironmentResponseMessage -> previous.getPreviousComponentResponse()
            is StimulusMessage -> throw IllegalStateException("No component response message found")
        }
    }
}

@JvmInline
value class MessageValue(val value: String) {
    override fun toString(): String {
        return value
    }
}
