package de.rwth.swc.interact.junit.jupiter

import de.rwth.swc.interact.observer.TestObserver
import de.rwth.swc.interact.observer.domain.ObservedMessage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@ExtendWith(InterACt::class)
internal class InterACtTestCases {

    @Test
    fun `does load component information from property file`() {
        val observation = TestObserver.getObservations().last()
        TestObserver.recordMessage(
            ObservedMessage(
                "REST",
                mapOf(),
                ObservedMessage.Type.STIMULUS,
                "{result:\"success\"}",
                false
            )
        )
        assertThat(observation.name).isEqualTo("interact-junit")
        assertThat(observation.version).isEqualTo("1.0.0")
    }

    @Test
    fun `should fail`() {
        val observation = TestObserver.getObservations().last()
        assertThat(observation.name).isEqualTo("interact-junit")
        assertThat(observation.version).isEqualTo("1.0.0")
        assertThat(true).isEqualTo(false)
    }

    @Test
    @DisplayName("changedTestName")
    fun `display name is used as test name`() {
        val observation = TestObserver.getObservations().last()
        assertThat(observation.abstractTestCaseInfo?.concreteTestCaseInfo?.name).isEqualTo("changedTestName")
        assertThat(observation.abstractTestCaseInfo?.concreteTestCaseInfo?.parameters).hasSize(0)
    }

    @ParameterizedTest(name = "changedTestName{0}")
    @CsvSource("Test")
    fun `display name is used with parameterized test and parameters are set`(name: String) {
        val observation = TestObserver.getObservations().last()
        assertThat(observation.abstractTestCaseInfo?.concreteTestCaseInfo?.name).isEqualTo("changedTestNameTest")
        assertThat(observation.abstractTestCaseInfo?.concreteTestCaseInfo?.parameters).hasSize(1)
        assertThat(observation.abstractTestCaseInfo?.concreteTestCaseInfo?.parameters?.get(0)).isEqualTo("\"Test\"")
    }

    @RepeatedTest(value = 1, name = "changedTestName" + RepeatedTest.CURRENT_REPETITION_PLACEHOLDER)
    @ExtendWith(TestStringParameterResolver::class)
    fun `display name is used with parameter resolver and parameters are set`(name: String) {
        val observation = TestObserver.getObservations().last()
        assertThat(observation.abstractTestCaseInfo?.concreteTestCaseInfo?.name).isEqualTo("changedTestName1")
        assertThat(observation.abstractTestCaseInfo?.concreteTestCaseInfo?.parameters).hasSize(1)
        assertThat(observation.abstractTestCaseInfo?.concreteTestCaseInfo?.parameters?.get(0)).isEqualTo("\"Test\"")
    }


    @TestFactory
    fun `display name is used with dynamic tests`(): Collection<DynamicTest> {
        return listOf(
            DynamicTest.dynamicTest("testName") {
                val observation = TestObserver.getObservations().last()
                assertThat(observation.abstractTestCaseInfo?.concreteTestCaseInfo?.name).isEqualTo("testName")
                assertThat(observation.abstractTestCaseInfo?.concreteTestCaseInfo?.parameters).hasSize(0)
            }
        )
    }
}

internal class TestStringParameterResolver : ParameterResolver {
    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        return parameterContext.parameter.type === String::class.java
    }

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any {
        return "Test"
    }

}