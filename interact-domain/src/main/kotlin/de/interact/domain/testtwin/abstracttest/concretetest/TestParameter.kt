package de.interact.domain.testtwin.abstracttest.concretetest

@JvmInline
value class TestParameter(val value: String?) {
    override fun toString(): String {
        return value ?: "null"
    }
}