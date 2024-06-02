package de.interact.domain.testtwin.abstracttest

import de.interact.domain.shared.AbstractTestId
import de.interact.domain.testtwin.abstracttest.concretetest.ConcreteTestCase

data class AbstractTestCase(
    val id: AbstractTestId,
    val identifier: AbstractTestCaseIdentifier
) {
    constructor(
        id: AbstractTestId,
        identifier: AbstractTestCaseIdentifier,
        templateFor: Set<ConcreteTestCase>
    ) : this(id, identifier) {
        this.templateFor = templateFor
    }

    var templateFor: Set<ConcreteTestCase> = setOf()
        internal set
}

@JvmInline
value class AbstractTestCaseIdentifier(val value: String) {
    override fun toString(): String {
        return value
    }
}