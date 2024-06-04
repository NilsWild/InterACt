package de.interact.domain.testtwin.abstracttest

import arrow.optics.optics
import de.interact.domain.shared.AbstractTestId
import de.interact.domain.shared.Entity
import de.interact.domain.testtwin.abstracttest.concretetest.ConcreteTestCase

@optics
data class AbstractTestCase(
    override val id: AbstractTestId,
    val identifier: AbstractTestCaseIdentifier,
    var templateFor: Set<ConcreteTestCase> = emptySet(),
    override val version: Long? = null
): Entity<AbstractTestId>(){
    companion object {}
}

@JvmInline
value class AbstractTestCaseIdentifier(val value: String) {
    override fun toString(): String {
        return value
    }
}