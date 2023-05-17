package de.rwth.swc.interact.junit.jupiter

import com.fasterxml.jackson.databind.ObjectMapper
import de.rwth.swc.interact.integrator.Integrator
import de.rwth.swc.interact.integrator.domain.TestCaseReference
import de.rwth.swc.interact.observer.TestObserver
import de.rwth.swc.interact.observer.domain.ObservedTestResult
import de.rwth.swc.interact.test.ExITConfiguration
import de.rwth.swc.interact.utils.TestMode
import org.junit.jupiter.api.extension.*
import java.lang.reflect.Method
import java.util.*
import kotlin.reflect.full.createInstance


class InterACt : InvocationInterceptor, AfterTestExecutionCallback, BeforeTestExecutionCallback {

    private val props = Properties()

    init {
        props.load(this.javaClass.classLoader.getResourceAsStream("interact.properties"))
    }

    override fun interceptTestMethod(
        invocation: InvocationInterceptor.Invocation<Void>,
        invocationContext: ReflectiveInvocationContext<Method>,
        extensionContext: ExtensionContext
    ) {
        observeTest(extensionContext, argumentsFrom(invocationContext))
        integrate(extensionContext, argumentsFrom(invocationContext))
        if (!skipInvocationIfNeeded(invocation)) {
            super.interceptTestMethod(invocation, invocationContext, extensionContext)
        }
    }

    override fun interceptTestTemplateMethod(
        invocation: InvocationInterceptor.Invocation<Void>,
        invocationContext: ReflectiveInvocationContext<Method>,
        extensionContext: ExtensionContext
    ) {
        observeTest(extensionContext, argumentsFrom(invocationContext))
        integrate(extensionContext, argumentsFrom(invocationContext))
        if (!skipInvocationIfNeeded(invocation)) {
            super.interceptTestTemplateMethod(invocation, invocationContext, extensionContext)
        }
    }

    override fun interceptDynamicTest(
        invocation: InvocationInterceptor.Invocation<Void>,
        invocationContext: DynamicTestInvocationContext,
        extensionContext: ExtensionContext
    ) {
        observeTest(extensionContext, listOf())
        integrate(extensionContext, listOf())
        if (!skipInvocationIfNeeded(invocation)) {
            super.interceptDynamicTest(invocation, invocationContext, extensionContext)
        }
    }

    private fun integrate(context: ExtensionContext, arguments: List<String>) {
        val c = getContextWithRequiredTestClass(context)
        val interactionExpectationId = Integrator.startTestCase(
            TestCaseReference(
                c.requiredTestClass.canonicalName, c.requiredTestMethod.name, context.displayName, arguments
            )
        )
        interactionExpectationId?.let { TestObserver.setTestedInteractionExpectation(it) }
    }

    private fun skipInvocationIfNeeded(invocation: InvocationInterceptor.Invocation<Void>): Boolean {
        if (ExITConfiguration.mode == TestMode.INTERACTION) {
            if (!Integrator.doesReplacementExist()) {
                invocation.skip()
                TestObserver.dropObservation()
                return true
            }
        }
        return false
    }

    private fun observeTest(context: ExtensionContext, arguments: List<String>) {
        val c = getContextWithRequiredTestClass(context)
        val annotation = c.requiredTestClass.getAnnotation(ComponentInformationLoader::class.java)
        val observer = TestObserver
        observer.componentInformationLoader = annotation?.value?.createInstance() ?: observer.componentInformationLoader
        observer.startObservation(c.requiredTestClass, c.requiredTestMethod.name, context.displayName, arguments)
    }

    private fun getContextWithRequiredTestClass(context: ExtensionContext): ExtensionContext {
        return if (context.testClass.isPresent) {
            context
        } else {
            getContextWithRequiredTestClass(context.parent.orElseThrow { RuntimeException("Could not retrieve test class!") })
        }
    }

    private fun argumentsFrom(context: ReflectiveInvocationContext<Method>): List<String> {
        return context.arguments.map { ObjectMapper().writeValueAsString(it) }
    }

    override fun afterTestExecution(context: ExtensionContext) {
        TestObserver.setTestResult(if (context.executionException.isEmpty) ObservedTestResult.SUCCESS else ObservedTestResult.FAILED)
        TestObserver.pushObservations()
    }

    override fun beforeTestExecution(context: ExtensionContext) {
        ExITConfiguration.mode =
            TestMode.valueOf(context.getConfigurationParameter("interact.mode").orElseGet { TestMode.UNIT.toString() })
        if (ExITConfiguration.mode == TestMode.INTERACTION) {
            Integrator.pullReplacements()
        }
    }

}