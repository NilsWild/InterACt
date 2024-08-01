package de.interact.junit.jupiter

import de.interact.domain.shared.TestState
import de.interact.domain.testobservation.config.Configuration
import de.interact.domain.testobservation.model.ConcreteTestCaseName
import de.interact.domain.testobservation.model.TestCaseParameter
import de.interact.domain.testobservation.model.TypeIdentifier
import de.interact.junit.jupiter.annotation.InterACtTest
import de.interact.junit.jupiter.annotation.Offset
import org.junit.jupiter.api.Named
import org.junit.jupiter.api.extension.*
import org.junit.platform.commons.util.AnnotationUtils
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class InterACtTestParameterResolver(
    private val methodContext: InterACtTestMethodContext,
    private val arguments: Array<Any?>,
    private val invocationIndex: Int,
    private val mode: TestMode
) : ParameterResolver, AfterTestExecutionCallback {

    companion object {
        private val NAMESPACE: ExtensionContext.Namespace =
            ExtensionContext.Namespace.create(InterACtTestParameterResolver::class.java)
    }

    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        val declaringExecutable = parameterContext.declaringExecutable
        val testMethod = extensionContext.testMethod.orElse(null)
        val parameterIndex = parameterContext.index

        // Not a @ParameterizedTest method?
        if (declaringExecutable != testMethod) {
            return false
        }

        if (methodContext.isAggregator(parameterIndex)) {
            return true
        }

        return parameterIndex < arguments.size

        // Else fallback to behavior for parameterized test methods without aggregators.
    }

    @Throws(ParameterResolutionException::class)
    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any? {
        return when (mode) {
            TestMode.UNIT -> {
                val args = if (parameterContext.annotatedElement.isAnnotationPresent(Offset::class.java)) {
                    val offset =
                        parameterContext.annotatedElement.getAnnotation(Offset::class.java).value - parameterContext.index
                    arguments.copyOfRange(offset, arguments.size)
                } else {
                    arguments
                }
                methodContext.resolve(parameterContext, extractPayloads(args), invocationIndex)
            }

            TestMode.INTERACTION -> try {
                resolveInteractionMessage(parameterContext, extractPayloads(arguments))
            } catch (ex: Exception) {
                val compInfo = ExtensionContextToTestInfoMapper.componentInformationLoader(extensionContext)
                val abstractTestCase = Configuration.observationManager!!.observation.addObservedComponent(
                    compInfo.getComponentName(),
                    compInfo.getComponentVersion()
                ).addAbstractTestCase(
                    ExtensionContextToTestInfoMapper.abstractTestCaseSource(extensionContext),
                    ExtensionContextToTestInfoMapper.abstractTestCaseName(extensionContext),
                    extensionContext.testMethod.get().parameterTypes.map { TypeIdentifier(it.canonicalName) }
                )
                val concreteTestCase =
                    abstractTestCase.addInteractionTest(
                        ConcreteTestCaseName(extensionContext.displayName),
                        extractPayloads(arguments).map { TestCaseParameter(it.toString()) }
                    )
                concreteTestCase.executionFinished(TestState.TestFinishedState.Failed.ExceptionFailed)
                throw ex
            }
        }
    }

    private fun resolveInteractionMessage(parameterContext: ParameterContext, arguments: Array<Any?>): Any? {
        val argument = arguments[parameterContext.index]
        return ParameterTypeResolver.resolveArgumentToParameterType(parameterContext, argument)
    }

    override fun afterTestExecution(context: ExtensionContext) {
        val parameterizedTest = AnnotationUtils.findAnnotation(
            context.requiredTestMethod,
            InterACtTest::class.java
        ).get()
        if (!parameterizedTest.autoCloseArguments) {
            return
        }
        val store = context.getStore(NAMESPACE)
        val argumentIndex = AtomicInteger()
        arguments
            .filter { obj -> AutoCloseable::class.java.isInstance(obj) }
            .map { obj -> AutoCloseable::class.java.cast(obj) }
            .map { autoCloseable -> CloseableArgument(autoCloseable) }
            .forEach { closeable -> store.put("closeableArgument#" + argumentIndex.incrementAndGet(), closeable) }
    }

    private class CloseableArgument(private val autoCloseable: AutoCloseable) :
        ExtensionContext.Store.CloseableResource {
        @Throws(Throwable::class)
        override fun close() {
            autoCloseable.close()
        }
    }

    private fun extractPayloads(arguments: Array<Any?>): Array<Any?> {
        return arguments
            .map { argument ->
                if (argument is Named<*>) {
                    return@map (argument).payload
                }
                argument
            }
            .toTypedArray()
    }

}