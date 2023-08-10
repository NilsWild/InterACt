package de.rwth.swc.interact.junit.jupiter

import com.fasterxml.jackson.databind.ObjectMapper
import de.rwth.swc.interact.domain.*
import de.rwth.swc.interact.observer.TestObserver
import de.rwth.swc.interact.test.ExampleBasedAssertionError
import de.rwth.swc.interact.test.annotation.ComponentInformationLoader
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.InvocationInterceptor
import org.junit.jupiter.api.extension.ReflectiveInvocationContext
import java.lang.reflect.Method
import kotlin.reflect.full.createInstance

class InterACtTestInvocationInterceptor(
    private val mode: TestMode,
    private val testInvocationDescriptor: TestInvocationDescriptor?
) : InvocationInterceptor {

    override fun interceptTestTemplateMethod(
        invocation: InvocationInterceptor.Invocation<Void>,
        invocationContext: ReflectiveInvocationContext<Method>,
        extensionContext: ExtensionContext
    ) {
        observeTest(extensionContext, argumentsFrom(invocationContext), mode)
        integrate(testInvocationDescriptor)
        try {
            super.interceptTestTemplateMethod(invocation, invocationContext, extensionContext)
        } catch (e: ExampleBasedAssertionError) {
            if (mode == TestMode.UNIT) {
                throw e
            }
        }
    }

    private fun observeTest(context: ExtensionContext, arguments: List<TestCaseParameter>, mode: TestMode) {
        val c = getContextWithRequiredTestClass(context)
        val annotation = c.requiredTestClass.getAnnotation(ComponentInformationLoader::class.java)
        val observer = TestObserver
        observer.componentInformationLoader = annotation?.value?.createInstance() ?: observer.componentInformationLoader
        observer.startObservation(
            c.requiredTestClass,
            AbstractTestCaseName(c.requiredTestMethod.name),
            ConcreteTestCaseName(context.displayName),
            arguments,
            mode
        )
    }

    private fun getContextWithRequiredTestClass(context: ExtensionContext): ExtensionContext {
        return if (context.testClass.isPresent) {
            context
        } else {
            getContextWithRequiredTestClass(context.parent.orElseThrow { RuntimeException("Could not retrieve test class!") })
        }
    }

    private fun integrate(testInvocationDescriptor: TestInvocationDescriptor?) {
        testInvocationDescriptor?.let { TestObserver.setTestedInteractionExpectation(it.interactionExpectationId) }
    }

    private fun argumentsFrom(context: ReflectiveInvocationContext<Method>): List<TestCaseParameter> {
        return context.arguments.map { TestCaseParameter(ObjectMapper().writeValueAsString(it)) }
    }
}