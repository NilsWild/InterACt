package de.interact.domain.shared

import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.util.*

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
sealed interface TestId {
    val value: UUID
}

@JvmInline
value class UnitTestId(override val value: UUID) : TestId {
    override fun toString(): String {
        return value.toString()
    }
}

@JvmInline
value class InteractionTestId(override val value: UUID) : TestId {
    override fun toString(): String {
        return value.toString()
    }
}