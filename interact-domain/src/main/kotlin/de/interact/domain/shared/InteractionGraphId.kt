package de.interact.domain.shared

import java.util.UUID

@JvmInline
value class InteractionGraphId(val value: UUID) {
    override fun toString(): String {
        return value.toString()
    }
}