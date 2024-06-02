package de.interact.domain.testobservation.model

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.mockk
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class AbstractTestCaseTest {

    @Nested
    inner class AddUnitTest {

        @ParameterizedTest
        @CsvSource(
            "source, abstractTestCaseName, concreteTestCaseName, parameter"
        )
        fun `addUnitTest should add test case and return the added instance`(
            source: AbstractTestCaseSource,
            abstractTestCaseName: AbstractTestCaseName,
            concreteTestCaseName: ConcreteTestCaseName,
            parameter: TestCaseParameter
        ) {
            val observedComponent: ObservedComponent = mockk()
            val abstractTestCase = AbstractTestCase(
                observedComponent,
                source,
                abstractTestCaseName,
                listOf(TypeIdentifier(String::class.java.canonicalName))
            )
            val unitTestCase = abstractTestCase.addUnitTest(concreteTestCaseName, listOf(parameter))

            assertSoftly {
                abstractTestCase.templateFor shouldHaveSize 1
                unitTestCase.derivedFrom shouldBeSameInstanceAs abstractTestCase
                unitTestCase.name shouldBe concreteTestCaseName
                unitTestCase.parameters shouldBe listOf(parameter)
            }
        }

        @ParameterizedTest
        @CsvSource(
            "source, abstractTestCaseName, concreteTestCaseName, parameter"
        )
        fun `addUnitTest should return contained instance when test case is added twice`(
            source: AbstractTestCaseSource,
            abstractTestCaseName: AbstractTestCaseName,
            concreteTestCaseName: ConcreteTestCaseName,
            parameter: TestCaseParameter
        ) {
            val observedComponent: ObservedComponent = mockk()
            val abstractTestCase = AbstractTestCase(
                observedComponent,
                source,
                abstractTestCaseName,
                listOf(TypeIdentifier(String::class.java.canonicalName))
            )
            val unitTestCase = abstractTestCase.addUnitTest(concreteTestCaseName, listOf(parameter))
            val unitTestCase2 = abstractTestCase.addUnitTest(concreteTestCaseName, listOf(parameter))

            assertSoftly {
                abstractTestCase.templateFor shouldHaveSize 1
                unitTestCase shouldBeSameInstanceAs unitTestCase2
            }
        }
    }

    @Nested
    inner class AddInteractionTest {

        @ParameterizedTest
        @CsvSource(
            "source, abstractTestCaseName, concreteTestCaseName, parameter"
        )
        fun `addInteractionTest should add test case and return the added instance`(
            source: AbstractTestCaseSource,
            abstractTestCaseName: AbstractTestCaseName,
            concreteTestCaseName: ConcreteTestCaseName,
            parameter: TestCaseParameter
        ) {
            val observedComponent: ObservedComponent = mockk()
            val abstractTestCase = AbstractTestCase(
                observedComponent,
                source,
                abstractTestCaseName,
                listOf(TypeIdentifier(String::class.java.canonicalName))
            )
            val interactionTestCase = abstractTestCase.addInteractionTest(concreteTestCaseName, listOf(parameter))

            assertSoftly {
                abstractTestCase.templateFor shouldHaveSize 1
                interactionTestCase.derivedFrom shouldBeSameInstanceAs abstractTestCase
                interactionTestCase.name shouldBe concreteTestCaseName
                interactionTestCase.parameters shouldBe listOf(parameter)
            }
        }

        @ParameterizedTest
        @CsvSource(
            "source, abstractTestCaseName, concreteTestCaseName, parameter"
        )
        fun `addInteractionTest should return contained instance when test case is added twice`(
            source: AbstractTestCaseSource,
            abstractTestCaseName: AbstractTestCaseName,
            concreteTestCaseName: ConcreteTestCaseName,
            parameter: TestCaseParameter
        ) {
            val observedComponent: ObservedComponent = mockk()
            val abstractTestCase = AbstractTestCase(
                observedComponent,
                source,
                abstractTestCaseName,
                listOf(TypeIdentifier(String::class.java.canonicalName))
            )
            val interactionTestCase = abstractTestCase.addInteractionTest(concreteTestCaseName, listOf(parameter))
            val interactionTestCase2 = abstractTestCase.addInteractionTest(concreteTestCaseName, listOf(parameter))

            assertSoftly {
                abstractTestCase.templateFor shouldHaveSize 1
                interactionTestCase shouldBeSameInstanceAs interactionTestCase2
            }
        }
    }
}