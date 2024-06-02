package de.interact.domain.shared

import java.util.*

@JvmInline
value class SystemPropertyExpectationId(val value: UUID) {
    override fun toString(): String {
        return value.toString()
    }
}