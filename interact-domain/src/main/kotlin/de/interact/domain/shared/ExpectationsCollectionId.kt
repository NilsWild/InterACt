package de.interact.domain.shared

import java.util.*

@JvmInline
value class ExpectationsCollectionId(val value: UUID) {
    override fun toString(): String {
        return value.toString()
    }
}