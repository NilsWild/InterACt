package de.rwth.swc.interact.utbi

import de.rwth.swc.interact.domain.ComponentInterface
import de.rwth.swc.interact.domain.InterfaceExpectation

interface InterfaceExpectationMatcher {
    val name: InterfaceExpectationMatcherName
    val version: InterfaceExpectationMatcherVersion

    fun canHandle(expectation: InterfaceExpectation): Boolean
    fun canHandle(componentInterface: ComponentInterface): Boolean
    fun match(expectation: InterfaceExpectation)
    fun match(componentInterface: ComponentInterface)
}

@JvmInline
value class InterfaceExpectationMatcherName(val name: String) {
    override fun toString(): String {
        return name
    }
}

@JvmInline
value class InterfaceExpectationMatcherVersion(val version: String) {
    override fun toString(): String {
        return version
    }
}
