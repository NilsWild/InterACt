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
sealed interface InterfaceId {
    val value: UUID
}

@JvmInline
value class IncomingInterfaceId(override val value: UUID) : InterfaceId {
    override fun toString(): String {
        return value.toString()
    }
}

@JvmInline
value class OutgoingInterfaceId(override val value: UUID) : InterfaceId {
    override fun toString(): String {
        return value.toString()
    }
}