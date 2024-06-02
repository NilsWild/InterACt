package de.interact.domain.expectations.shared

@JvmInline
value class MessageValue(val value: String?) {
    override fun toString(): String {
        return value ?: "null"
    }
}