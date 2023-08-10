package de.rwth.swc.interact.junit.jupiter

import de.rwth.swc.interact.domain.AbstractTestCase
import de.rwth.swc.interact.domain.AbstractTestCaseName
import de.rwth.swc.interact.domain.AbstractTestCaseSource
import de.rwth.swc.interact.domain.TestInvocationDescriptor
import de.rwth.swc.interact.integrator.Integrator
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import java.util.stream.Stream

class InterACtArgumentsProvider {

    fun provideArguments(context: ExtensionContext): Stream<Pair<TestInvocationDescriptor, Arguments>> {
        val c = getContextWithRequiredTestClass(context)
        val abstractTestCase = AbstractTestCase(
            AbstractTestCaseSource(c.requiredTestClass.canonicalName), AbstractTestCaseName(c.requiredTestMethod.name)
        )
        val testCases = Integrator.interactionTestCases
        return testCases.firstOrNull { it.abstractTestCase == abstractTestCase }?.testInvocations?.mapIndexed { index, invocation ->
            Pair(
                invocation,
                Arguments.of(
                    *fillWithNull(
                        invocation.messages.map { it.value }.toTypedArray(),
                        context.requiredTestMethod.parameters.size
                    )
                )
            )
        }?.stream() ?: Stream.empty()
    }

    private fun fillWithNull(arguments: Array<String>, size: Int): Array<String?> {
        return arguments.copyOf(size)
    }

    private fun getContextWithRequiredTestClass(context: ExtensionContext): ExtensionContext {
        return if (context.testClass.isPresent) {
            context
        } else {
            getContextWithRequiredTestClass(context.parent.orElseThrow { RuntimeException("Could not retrieve test class!") })
        }
    }

}