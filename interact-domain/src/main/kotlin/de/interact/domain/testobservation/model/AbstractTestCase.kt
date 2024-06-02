package de.interact.domain.testobservation.model

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators

@JsonIdentityInfo(
    generator = ObjectIdGenerators.UUIDGenerator::class,
    property = "@id"
)
data class AbstractTestCase(
    val component: ObservedComponent,
    val source: AbstractTestCaseSource,
    val name: AbstractTestCaseName,
    val parameterTypes: List<TypeIdentifier>
) {
    var templateFor = setOf<ConcreteTestCase>()
        private set

    fun addUnitTest(name: ConcreteTestCaseName, parameters: List<TestCaseParameter>): UnitTestCase {
        require(parameters.size == parameterTypes.size) {
            "Number of parameters does not match number of parameter types"
        }
        val concreteTestCase = UnitTestCase(this, name, parameters)
        val result = templateFor.firstOrNull { it == concreteTestCase }
        return if (result != null) {
            result as UnitTestCase
        } else {
            templateFor += concreteTestCase
            concreteTestCase
        }
    }

    fun addInteractionTest(name: ConcreteTestCaseName, parameters: List<TestCaseParameter>): InteractionTestCase {
        require(parameters.size == parameterTypes.size) {
            "Number of parameters does not match number of parameter types"
        }
        val concreteTestCase = InteractionTestCase(this, name, parameters)
        val result = templateFor.firstOrNull { it == concreteTestCase }
        return if (result != null) {
            result as InteractionTestCase
        } else {
            templateFor += concreteTestCase
            concreteTestCase
        }
    }
}

@JvmInline
value class TypeIdentifier(val value: String) {
    override fun toString(): String {
        return value
    }
}

@JvmInline
value class AbstractTestCaseName(val value: String) {
    override fun toString(): String {
        return value
    }
}

@JvmInline
value class AbstractTestCaseSource(val value: String) {
    override fun toString(): String {
        return value
    }
}