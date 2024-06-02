package de.interact.domain.testobservation.model

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators

@JsonIdentityInfo(
    generator = ObjectIdGenerators.UUIDGenerator::class,
    property = "@id"
)
data class ObservedComponent(
    val name: ComponentName,
    val version: ComponentVersion
) {

    var testedBy: Set<AbstractTestCase> = emptySet()
        private set

    fun addAbstractTestCase(
        source: AbstractTestCaseSource,
        name: AbstractTestCaseName,
        parameterTypes: List<TypeIdentifier>
    ): AbstractTestCase {
        val abstractTestCase = AbstractTestCase(this, source, name, parameterTypes)
        val result = testedBy.firstOrNull { it == abstractTestCase }
        return if (result != null) {
            result
        } else {
            testedBy += abstractTestCase
            abstractTestCase
        }
    }
}

@JvmInline
value class ComponentName(val value: String) {
    override fun toString(): String {
        return value
    }
}

@JvmInline
value class ComponentVersion(val value: String) {
    override fun toString(): String {
        return value
    }
}