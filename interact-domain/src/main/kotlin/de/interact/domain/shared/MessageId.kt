package de.interact.domain.shared

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import java.util.*

@JsonIdentityInfo(
    generator = ObjectIdGenerators.UUIDGenerator::class,
    property = "@id"
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type")
sealed interface MessageId {
    val value: UUID
}

sealed interface SentMessageId : MessageId
sealed interface ReceivedMessageId : MessageId

@JvmInline
value class StimulusMessageId(override val value: UUID) : ReceivedMessageId {
    override fun toString(): String {
        return value.toString()
    }
}

@JvmInline
value class ComponentResponseMessageId(override val value: UUID) : SentMessageId {
    override fun toString(): String {
        return value.toString()
    }
}

@JvmInline
value class EnvironmentResponseMessageId(override val value: UUID) : ReceivedMessageId {
    override fun toString(): String {
        return value.toString()
    }
}