package de.interact.domain.shared

import java.util.*

sealed interface InteractionExpectationId {
    val value: UUID
}

@JvmInline
value class UnitTestBasedInteractionExpectationId(override val value: UUID) : InteractionExpectationId {
    override fun toString(): String {
        return value.toString()
    }
}

@JvmInline
value class SystemInteractionExpectationId(override val value: UUID) : InteractionExpectationId {
    override fun toString(): String {
        return value.toString()
    }
}