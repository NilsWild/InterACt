package de.rwth.swc.interact.junit.jupiter

import com.fasterxml.jackson.databind.ObjectMapper
import de.rwth.swc.interact.domain.*
import de.rwth.swc.interact.integrator.Integrator
import de.rwth.swc.interact.observer.TestObserver
import de.rwth.swc.interact.test.ExITConfiguration
import de.rwth.swc.interact.test.annotation.ComponentInformationLoader
import org.junit.jupiter.api.extension.*
import java.lang.reflect.Method
import java.util.*
import kotlin.reflect.full.createInstance


/**
 * The InterACt junit extension is used to observe the test execution and record the observed messages
 * as well as to manipulate the unit tests to use the messages of test executions of other components.
 */
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
        integrate(extensionContext)
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
        integrate(extensionContext)
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
        integrate(extensionContext)
        if (!skipInvocationIfNeeded(invocation)) {
            super.interceptDynamicTest(invocation, invocationContext, extensionContext)
        }
    }

    private fun integrate(context: ExtensionContext) {
        val c = getContextWithRequiredTestClass(context)
        val interactionExpectationId = Integrator.startTestCase(
            AbstractTestCase(
                AbstractTestCaseSource(c.requiredTestClass.canonicalName), AbstractTestCaseName(c.requiredTestMethod.name)
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

    private fun observeTest(context: ExtensionContext, arguments: List<TestCaseParameter>) {
        val c = getContextWithRequiredTestClass(context)
        val annotation = c.requiredTestClass.getAnnotation(ComponentInformationLoader::class.java)
        val observer = TestObserver
        observer.componentInformationLoader = annotation?.value?.createInstance() ?: observer.componentInformationLoader
        observer.startObservation(c.requiredTestClass, AbstractTestCaseName(c.requiredTestMethod.name), ConcreteTestCaseName(context.displayName), arguments)
    }

    private fun getContextWithRequiredTestClass(context: ExtensionContext): ExtensionContext {
        return if (context.testClass.isPresent) {
            context
        } else {
            getContextWithRequiredTestClass(context.parent.orElseThrow { RuntimeException("Could not retrieve test class!") })
        }
    }

    private fun argumentsFrom(context: ReflectiveInvocationContext<Method>): List<TestCaseParameter> {
        return context.arguments.map { TestCaseParameter(ObjectMapper().writeValueAsString(it)) }
    }

    override fun afterTestExecution(context: ExtensionContext) {
        TestObserver.setTestResult(if (context.executionException.isEmpty) TestResult.SUCCESS else TestResult.FAILED)
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