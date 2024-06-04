package de.interact.domain.testtwin.abstracttest.concretetest

import arrow.optics.optics
import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import de.interact.domain.shared.*
import de.interact.domain.testtwin.abstracttest.concretetest.message.Message
import java.util.*
import de.interact.domain.testobservation.model.ConcreteTestCase as ObservedTestCase

@JsonIdentityInfo(
    generator = ObjectIdGenerators.UUIDGenerator::class,
    property = "@id"
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type")
@optics
sealed class ConcreteTestCase: Entity<TestId>(){
    abstract val identifier: ConcreteTestCaseIdentifier
    abstract val parameters: List<TestParameter>
    abstract val triggeredMessages: SortedSet<Message>
    abstract val status: TestState

    companion object {}
}

@optics
data class UnitTest(
    override val id: UnitTestId,
    override val identifier: ConcreteTestCaseIdentifier,
    override val parameters: List<TestParameter>,
    override val triggeredMessages: SortedSet<Message>,
    override val status: TestState,
    override val version: Long? = null
) : ConcreteTestCase() {
    companion object {}
}

@optics
data class InteractionTest(
    override val id: InteractionTestId,
    override val identifier: ConcreteTestCaseIdentifier,
    override val parameters: List<TestParameter>,
    override val triggeredMessages: SortedSet<Message>,
    override val status: TestState,
    override val version: Long? = null
) : ConcreteTestCase() {
    companion object {}
}

@JvmInline
value class ConcreteTestCaseIdentifier(val value: String) {
    override fun toString(): String {
        return value
    }
}

fun ObservedTestCase.testCaseIdentifier() = ConcreteTestCaseIdentifier("${this.name}:${this.parameters}")