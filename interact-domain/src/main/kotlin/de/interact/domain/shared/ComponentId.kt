package de.interact.domain.shared

import java.util.*

@JvmInline
value class ComponentId(val value: UUID) {
    override fun toString(): String {
        return value.toString()
    }
}