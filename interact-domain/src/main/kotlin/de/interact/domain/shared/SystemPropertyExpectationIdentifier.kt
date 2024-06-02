package de.interact.domain.shared

@JvmInline
value class SystemPropertyExpectationIdentifier(val value: String) {
    override fun toString(): String {
        return value
    }
}