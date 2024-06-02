package de.interact.domain.expectations

@JvmInline
value class TestParameter(val value: String?) {
    override fun toString(): String {
        return value ?: "null"
    }
}