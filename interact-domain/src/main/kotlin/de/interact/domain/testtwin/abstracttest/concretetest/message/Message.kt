package de.interact.domain.testtwin.abstracttest.concretetest.message

import arrow.optics.optics
import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import de.interact.domain.shared.*

@JsonIdentityInfo(
    generator = ObjectIdGenerators.UUIDGenerator::class,
    property = "@id"
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type")
@optics
sealed class Message : Entity<MessageId>(), Comparable<Message> {

    abstract val value: MessageValue
    abstract val order: Int

    companion object;

    @optics
    sealed class SentMessage : Message() {
        abstract override val id: SentMessageId
        abstract val sentBy: EntityReference<OutgoingInterfaceId>
        abstract val dependsOn: Collection<EntityReference<ReceivedMessageId>>

        companion object
    }

    @optics
    sealed class ReceivedMessage : Message() {
        abstract override val id: ReceivedMessageId
        abstract val receivedBy: EntityReference<IncomingInterfaceId>

        companion object
    }

    override fun compareTo(other: Message): Int {
        return order - other.order
    }
}

@optics
data class StimulusMessage(
    override val id: StimulusMessageId,
    override val value: MessageValue,
    override val receivedBy: EntityReference<IncomingInterfaceId>,
    override val version: Long? = null
) : Message.ReceivedMessage() {
    override val order = 0

    companion object
}

@optics
data class ComponentResponseMessage(
    override val id: ComponentResponseMessageId,
    override val value: MessageValue,
    override val order: Int,
    override val sentBy: EntityReference<OutgoingInterfaceId>,
    override val dependsOn: Collection<EntityReference<ReceivedMessageId>>,
    override val version: Long? = null
) : Message.SentMessage() {
    companion object
}

@optics
data class EnvironmentResponseMessage(
    override val id: EnvironmentResponseMessageId,
    override val value: MessageValue,
    override val order: Int,
    override val receivedBy: EntityReference<IncomingInterfaceId>,
    val reactionTo: EntityReference<ComponentResponseMessageId>,
    override val version: Long? = null
) : Message.ReceivedMessage() {
    companion object
}

@JvmInline
value class MessageValue(val value: String) {
    override fun toString(): String {
        return value
    }
}