package de.interact.junit.jupiter

import de.interact.domain.testobservation.config.Configuration
import de.interact.domain.testobservation.model.ConcreteTestCaseName
import de.interact.domain.shared.TestState
import de.interact.domain.testobservation.model.TypeIdentifier
import de.interact.test.ExampleBasedAssertionError
import org.awaitility.Awaitility
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.InvocationInterceptor
import org.junit.jupiter.api.extension.ReflectiveInvocationContext
import java.lang.reflect.Method

class InterACtTestInvocationInterceptor(
    private val mode: TestMode
) : InvocationInterceptor {

    override fun interceptTestTemplateMethod(
        invocation: InvocationInterceptor.Invocation<Void>,
        invocationContext: ReflectiveInvocationContext<Method>,
        extensionContext: ExtensionContext
    ) {
        val compInfo = ExtensionContextToTestInfoMapper.componentInformationLoader(extensionContext)
        val abstractTestCase = Configuration.observationManager!!.observation.addObservedComponent(
            compInfo.getComponentName(),
            compInfo.getComponentVersion()
        ).addAbstractTestCase(
            ExtensionContextToTestInfoMapper.abstractTestCaseSource(extensionContext),
            ExtensionContextToTestInfoMapper.abstractTestCaseName(extensionContext),
            invocationContext.executable.parameterTypes.map { TypeIdentifier(it.canonicalName) }
        )
        val concreteTestCase =
            when (mode) {
                TestMode.UNIT -> abstractTestCase.addUnitTest(
                    ConcreteTestCaseName(extensionContext.displayName),
                    ExtensionContextToTestInfoMapper.argumentsFrom(invocationContext)
                )

                TestMode.INTERACTION -> abstractTestCase.addInteractionTest(
                    ConcreteTestCaseName(extensionContext.displayName),
                    ExtensionContextToTestInfoMapper.argumentsFrom(invocationContext)
                )
            }
        Configuration.observationManager!!.observeTestCase(concreteTestCase) {
            try {
                super.interceptTestTemplateMethod(invocation, invocationContext, extensionContext)
                concreteTestCase.executionFinished(TestState.TestFinishedState.Succeeded)
            } catch (e: ExampleBasedAssertionError) {
                concreteTestCase.executionFinished(TestState.TestFinishedState.Failed.AssertionFailed)
                if (mode == TestMode.UNIT) {
                    throw e
                }
            } catch (e: AssertionError) {
                concreteTestCase.executionFinished(TestState.TestFinishedState.Failed.AssertionFailed)
                throw e
            } catch (e: Exception) {
                concreteTestCase.executionFinished(TestState.TestFinishedState.Failed.ExceptionFailed)
                throw e
            }
            Awaitility.await().until {
                Configuration.observationManager!!.testFinished()
            }
        }
    }
}