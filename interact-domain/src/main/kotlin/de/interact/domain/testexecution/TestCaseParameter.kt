package de.interact.domain.testexecution

@JvmInline
value class TestCaseParameter(val value: String?) {
    override fun toString(): String {
        return value ?: "null"
    }
}