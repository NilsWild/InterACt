package de.interact.domain.testobservation.model

import de.interact.domain.shared.TestState
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class ConcreteTestCaseTest {

    @Nested
    inner class ExecutionFinished {

        @ParameterizedTest
        @CsvSource(
            "concreteTestCaseName, parameter, Succeeded"
        )
        fun `execution finished should update state`(
            concreteTestCaseName: ConcreteTestCaseName,
            parameter: TestCaseParameter
        ) {
            val abstractTestCase: AbstractTestCase = mockk()
            val testCase = UnitTestCase(abstractTestCase, concreteTestCaseName, listOf(parameter))
                .apply {
                    executionFinished(TestState.TestFinishedState.Succeeded)
                }

            assertSoftly(testCase) {
                status shouldBe TestState.TestFinishedState.Succeeded
            }
        }

        @ParameterizedTest
        @CsvSource(
            "concreteTestCaseName, parameter"
        )
        fun `execution finished should throw state exception when test case is already finished`(
            concreteTestCaseName: ConcreteTestCaseName,
            parameter: TestCaseParameter
        ) {
            val abstractTestCase: AbstractTestCase = mockk()
            val testCase = UnitTestCase(abstractTestCase, concreteTestCaseName, listOf(parameter))
                .apply {
                    executionFinished(TestState.TestFinishedState.Succeeded)
                }
            shouldThrowExactly<IllegalStateException> {
                testCase.executionFinished(TestState.TestFinishedState.Succeeded)
            }
        }
    }
}