package de.rwth.swc.interact.junit.jupiter

import de.rwth.swc.interact.domain.TestMode
import de.rwth.swc.interact.junit.jupiter.annotation.InterACtTest
import de.rwth.swc.interact.junit.jupiter.annotation.Offset
import io.github.projectmapk.jackson.module.kogera.jacksonObjectMapper
import org.junit.jupiter.api.Named
import org.junit.jupiter.api.extension.*
import org.junit.jupiter.params.converter.DefaultArgumentConverter
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
            TestMode.INTERACTION -> resolveMessage(parameterContext, extractPayloads(arguments))
        }
    }

    private fun resolveMessage(parameterContext: ParameterContext, arguments: Array<Any?>): Any? {
        val argument = arguments[parameterContext.index]
        return if (argument == null) {
            null
        } else if (parameterContext.parameter.type == argument.javaClass) {
            argument
        } else if (parameterContext.parameter.type.isPrimitive) {
            DefaultArgumentConverter.INSTANCE.convert(argument, parameterContext)
        } else {
            jacksonObjectMapper().readValue(argument.toString(), parameterContext.parameter.type)
        }
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