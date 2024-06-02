package de.interact.domain.testtwin.abstracttest.concretetest

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import de.interact.domain.shared.InteractionTestId
import de.interact.domain.shared.TestId
import de.interact.domain.shared.UnitTestId
import de.interact.domain.shared.TestState
import de.interact.domain.testtwin.abstracttest.concretetest.message.Message
import java.util.*
import de.interact.domain.testobservation.model.ConcreteTestCase as ObservedTestCase

@JsonIdentityInfo(
    generator = ObjectIdGenerators.UUIDGenerator::class,
    property = "@id"
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type")
sealed class ConcreteTestCase(
    open val id: TestId,
    open val identifier: ConcreteTestCaseIdentifier,
    open val parameters: List<TestParameter>,
    open val triggeredMessages: SortedSet<Message>,
    open val status: TestState,
    open val version: Long? = null
)

data class UnitTest(
    override val id: UnitTestId,
    override val identifier: ConcreteTestCaseIdentifier,
    override val parameters: List<TestParameter>,
    override val triggeredMessages: SortedSet<Message>,
    override val status: TestState,
    override val version: Long? = null
) : ConcreteTestCase(id, identifier, parameters, triggeredMessages, status)

data class InteractionTest(
    override val id: InteractionTestId,
    override val identifier: ConcreteTestCaseIdentifier,
    override val parameters: List<TestParameter>,
    override val triggeredMessages: SortedSet<Message>,
    override val status: TestState,
    override val version: Long? = null
) : ConcreteTestCase(id, identifier, parameters, triggeredMessages, status)

@JvmInline
value class ConcreteTestCaseIdentifier(val value: String) {
    override fun toString(): String {
        return value
    }
}

fun ObservedTestCase.testCaseIdentifier() = ConcreteTestCaseIdentifier("${this.name}:${this.parameters}")