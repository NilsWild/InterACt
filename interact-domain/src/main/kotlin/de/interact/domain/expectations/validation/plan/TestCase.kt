package de.interact.domain.expectations.validation.plan

import de.interact.domain.expectations.TestParameter
import de.interact.domain.shared.*
import java.util.*

sealed class TestCase: Entity<TestDefinitionId>() {

    abstract val derivedFrom: EntityReference<AbstractTestId>
    abstract val replacements: Set<Replacement>

    fun clone(): TestCase {
        return when(this) {
            is IncompleteTestCase -> this.copy(id = TestDefinitionId(UUID.randomUUID()), replacements = this.replacements.map { it.clone() }.toSet())
            is ExecutableTestCase -> this.copy(id = TestDefinitionId(UUID.randomUUID()), replacements = this.replacements.map { it.clone() }.toSet())
            is CompleteTestCase.Failed -> this.copy(id = TestDefinitionId(UUID.randomUUID()), replacements = this.replacements.map { it.clone() }.toSet())
            is CompleteTestCase.Succeeded -> this.copy(id = TestDefinitionId(UUID.randomUUID()), replacements = this.replacements.map { it.clone() }.toSet())
        }
    }

    data class IncompleteTestCase(
        override val derivedFrom: EntityReference<AbstractTestId>,
        override val replacements: Set<Replacement>,
        override val id: TestDefinitionId = TestDefinitionId(UUID.randomUUID()),
        override val version: Long? = null
    ) : TestCase()

    data class ExecutableTestCase(
        override val derivedFrom: EntityReference<AbstractTestId>,
        override val replacements: Set<Replacement>,
        val parameters: List<TestParameter>,
        override val id: TestDefinitionId = TestDefinitionId(UUID.randomUUID()),
        override val version: Long? = null
    ) : TestCase()

    sealed class CompleteTestCase: TestCase() {
        abstract val parameters: List<TestParameter>
        abstract val actualTest: EntityReference<TestId>

        data class Failed(
            override val derivedFrom: EntityReference<AbstractTestId>,
            override val replacements: Set<Replacement>,
            override val parameters: List<TestParameter>,
            override val actualTest: EntityReference<TestId>,
            override val id: TestDefinitionId = TestDefinitionId(UUID.randomUUID()),
            override val version: Long? = null
        ): CompleteTestCase()

        data class Succeeded(
            override val derivedFrom: EntityReference<AbstractTestId>,
            override val replacements: Set<Replacement>,
            override val parameters: List<TestParameter>,
            override val actualTest: EntityReference<TestId>,
            override val id: TestDefinitionId = TestDefinitionId(UUID.randomUUID()),
            override val version: Long? = null
        ): CompleteTestCase()
    }
}