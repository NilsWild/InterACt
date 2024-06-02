package de.interact.domain.testobservation.model

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import de.interact.domain.shared.TestState

@JsonIdentityInfo(
    generator = ObjectIdGenerators.UUIDGenerator::class,
    property = "@id"
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type")
@JsonSubTypes(
    JsonSubTypes.Type(UnitTestCase::class),
    JsonSubTypes.Type(InteractionTestCase::class)
)
sealed class ConcreteTestCase {
    abstract val derivedFrom: AbstractTestCase
    abstract val name: ConcreteTestCaseName
    abstract val parameters: List<TestCaseParameter>
    var status: TestState = TestState.NotExecuted
        private set
    val observedBehavior: ObservedBehavior = ObservedBehavior(this)

    fun executionFinished(state: TestState.TestFinishedState) {
        if (this.status != TestState.NotExecuted) {
            throw IllegalStateException("Test case already executed")
        }
        this.status = state
    }
}

data class UnitTestCase(
    override val derivedFrom: AbstractTestCase,
    override val name: ConcreteTestCaseName,
    override val parameters: List<TestCaseParameter>
) : ConcreteTestCase()

data class InteractionTestCase(
    override val derivedFrom: AbstractTestCase,
    override val name: ConcreteTestCaseName,
    override val parameters: List<TestCaseParameter>
) : ConcreteTestCase()

@JvmInline
value class ConcreteTestCaseName(val value: String) {
    override fun toString(): String {
        return value
    }
}

@JvmInline
value class TestCaseParameter(val value: String?) {
    override fun toString(): String {
        return value ?: "null"
    }
}