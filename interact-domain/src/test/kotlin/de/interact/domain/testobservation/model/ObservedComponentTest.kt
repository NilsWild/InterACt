package de.interact.domain.testobservation.model

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class ObservedComponentTest {

    @ParameterizedTest
    @CsvSource(
        "componentName, componentVersion"
    )
    fun `should initialize correctly`(name: ComponentName, version: ComponentVersion) {
        val observedComponent = ObservedComponent(name, version)

        observedComponent.name shouldBe name
        observedComponent.version shouldBe version
        observedComponent.testedBy shouldHaveSize 0
    }

    @Nested
    inner class AddAbstractTestCase {
        @ParameterizedTest
        @CsvSource(
            "componentName, componentVersion, source, abstractTestCaseName"
        )
        fun `addAbstractTestCase should add the test case and return the added instance`(
            name: ComponentName,
            version: ComponentVersion,
            source: AbstractTestCaseSource,
            abstractTestCaseName: AbstractTestCaseName
        ) {
            val observedComponent = ObservedComponent(name, version).apply {
                addAbstractTestCase(source, abstractTestCaseName, listOf())
            }

            assertSoftly {
                observedComponent.testedBy shouldHaveSize 1
                observedComponent.testedBy.first().component shouldBeSameInstanceAs observedComponent
                observedComponent.testedBy.first().source shouldBe source
                observedComponent.testedBy.first().name shouldBe abstractTestCaseName
            }
        }

        @ParameterizedTest
        @CsvSource(
            "componentName, componentVersion, source, abstractTestCaseName"
        )
        fun `addAbstractTestCase should return contained instance when test case is added twice`(
            name: ComponentName,
            version: ComponentVersion,
            source: AbstractTestCaseSource,
            abstractTestCaseName: AbstractTestCaseName
        ) {
            val observedComponent = ObservedComponent(name, version)

            val abstractTestCase = observedComponent.addAbstractTestCase(source, abstractTestCaseName, listOf())
            val abstractTestCase2 = observedComponent.addAbstractTestCase(source, abstractTestCaseName, listOf())

            assertSoftly {
                abstractTestCase shouldBeSameInstanceAs abstractTestCase2
                observedComponent.testedBy shouldHaveSize 1
                observedComponent.testedBy.first() shouldBeSameInstanceAs abstractTestCase
            }
        }
    }
}