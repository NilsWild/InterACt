package de.rwth.swc.interact.domain

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.util.*

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = SentMessage::class, name = "sent"),
    JsonSubTypes.Type(value = ReceivedMessage::class, name = "received"),
)
sealed class Message {
    abstract var id: MessageId?
    abstract val value: MessageValue
    abstract val originalMessageId: MessageId?
    abstract val messageType: MessageType
}

data class SentMessage(
    override val messageType: MessageType.Sent,
    override val value: MessageValue,
    val sentBy: OutgoingInterface,
    override val originalMessageId: MessageId? = null
) : Message() {
    override var id: MessageId? = null
}

fun sentBy(
    Protocol: Protocol,
    ProtocolData: ProtocolData
) = OutgoingInterface(Protocol, ProtocolData)

data class ReceivedMessage(
    override val messageType: MessageType.Received,
    override val value: MessageValue,
    val receivedBy: IncomingInterface,
    val isParameter: Boolean = false,
    override val originalMessageId: MessageId? = null
) : Message() {
    override var id: MessageId? = null
}

fun receivedBy(
    Protocol: Protocol,
    ProtocolData: ProtocolData
) = IncomingInterface(Protocol, ProtocolData)

sealed interface MessageType {
    enum class Received : MessageType {
        STIMULUS,
        ENVIRONMENT_RESPONSE
    }

    enum class Sent : MessageType {
        COMPONENT_RESPONSE
    }
}

@JvmInline
value class MessageValue(val value: String) {
    override fun toString(): String {
        return value
    }
}

@JvmInline
value class Protocol(val protocol: String) {
    override fun toString(): String {
        return protocol
    }
}

@JvmInline
value class ProtocolData(val data: Map<String, String>) {
    override fun toString(): String {
        return data.toString()
    }
}

@JvmInline
value class MessageId(val id: UUID) {
    override fun toString(): String {
        return id.toString()
    }

    companion object {
        fun random() = MessageId(UUID.randomUUID())
    }
}