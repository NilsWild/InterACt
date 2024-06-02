package de.interact.domain.shared

import java.util.UUID

@JvmInline
value class ReplacementId(val value: UUID) {
    override fun toString(): String {
        return value.toString()
    }
}
