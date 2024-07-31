package de.interact.domain.expectations.validation.plan

import de.interact.domain.expectations.validation.test.Test
import de.interact.domain.shared.*
import java.util.*

sealed class Interaction: Entity<InteractionId>() {
    abstract val derivedFrom: EntityReference<UnitTestId>
    abstract val testCase: TestCase
    abstract val from: Set<EntityReference<IncomingInterfaceId>>
    abstract val to: Set<EntityReference<OutgoingInterfaceId>>

    fun clone(): Interaction {
        return when(this) {
            is Pending -> this.copy(id = InteractionId(UUID.randomUUID()), testCase = this.testCase.clone() as TestCase.IncompleteTestCase)
            is Executable -> this.copy(id = InteractionId(UUID.randomUUID()), testCase = this.testCase.clone() as TestCase.ExecutableTestCase)
            is Finished.Validated -> this.copy(id = InteractionId(UUID.randomUUID()), testCase = this.testCase.clone() as TestCase.CompleteTestCase.Succeeded)
            is Finished.Failed -> this.copy(id = InteractionId(UUID.randomUUID()), testCase = this.testCase.clone() as TestCase.CompleteTestCase.Failed)
        }
    }

    data class Pending (
        override val derivedFrom: EntityReference<UnitTestId>,
        override val testCase: TestCase.IncompleteTestCase,
        override val from: Set<EntityReference<IncomingInterfaceId>>,
        override val to: Set<EntityReference<OutgoingInterfaceId>>,
        override val id: InteractionId = InteractionId(UUID.randomUUID()),
        override val version: Long? = null
    ): Interaction()

    data class Executable (
        override val derivedFrom: EntityReference<UnitTestId>,
        override val testCase: TestCase.ExecutableTestCase,
        override val from: Set<EntityReference<IncomingInterfaceId>>,
        override val to: Set<EntityReference<OutgoingInterfaceId>>,
        override val id: InteractionId = InteractionId(UUID.randomUUID()),
        override val version: Long? = null
    ): Interaction()

    sealed class Finished: Interaction() {

        data class Validated(
            override val derivedFrom: EntityReference<UnitTestId>,
            override val testCase: TestCase.CompleteTestCase.Succeeded,
            override val from: Set<EntityReference<IncomingInterfaceId>>,
            override val to: Set<EntityReference<OutgoingInterfaceId>>,
            override val id: InteractionId = InteractionId(UUID.randomUUID()),
            override val version: Long? = null
        ): Finished()

        data class Failed(
            override val derivedFrom: EntityReference<UnitTestId>,
            override val testCase: TestCase.CompleteTestCase.Failed,
            override val from: Set<EntityReference<IncomingInterfaceId>>,
            override val to: Set<EntityReference<OutgoingInterfaceId>>,
            override val id: InteractionId = InteractionId(UUID.randomUUID()),
            override val version: Long? = null
        ): Finished()
    }
}

internal fun Interaction.Executable.validate(test: Test): Interaction {
    return when(test.status) {
        is TestState.TestFinishedState.Succeeded -> Interaction.Finished.Validated(
            this.derivedFrom,
            TestCase.CompleteTestCase.Succeeded(
                this.testCase.derivedFrom,
                this.testCase.replacements,
                this.testCase.parameters,
                EntityReference(test.id, test.version),
                this.testCase.id,
                this.testCase.version
            ),
            this.from,
            this.to,
            this.id,
            this.version
        )
        is TestState.TestFinishedState.Failed -> Interaction.Finished.Failed(
            this.derivedFrom,
            TestCase.CompleteTestCase.Failed(
                this.testCase.derivedFrom,
                this.testCase.replacements,
                this.testCase.parameters,
                EntityReference(test.id, test.version),
                this.testCase.id,
                this.testCase.version
            ),
            this.from,
            this.to,
            this.id,
            this.version
        )
        else -> this
    }
}
