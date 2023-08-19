package de.rwth.swc.interact.junit.jupiter

import de.rwth.swc.interact.domain.AbstractTestCase
import de.rwth.swc.interact.domain.AbstractTestCaseName
import de.rwth.swc.interact.domain.AbstractTestCaseSource
import de.rwth.swc.interact.integrator.Integrator
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import java.util.stream.Stream

class InterACtArgumentsProvider {

    fun provideArguments(context: ExtensionContext): Stream<Arguments> {
        val c = getContextWithRequiredTestClass(context)
        val abstractTestCase = AbstractTestCase(
            AbstractTestCaseSource(c.requiredTestClass.canonicalName), AbstractTestCaseName(c.requiredTestMethod.name)
        )
        val testCases = Integrator.interactionTestCases
        return testCases.filter { it.abstractTestCase == abstractTestCase }.map { invocation ->
            Arguments.of(
                *invocation.parameters.map { it?.value }.toTypedArray()
            )
        }.stream()
    }

    private fun getContextWithRequiredTestClass(context: ExtensionContext): ExtensionContext {
        return if (context.testClass.isPresent) {
            context
        } else {
            getContextWithRequiredTestClass(context.parent.orElseThrow { RuntimeException("Could not retrieve test class!") })
        }
    }

}