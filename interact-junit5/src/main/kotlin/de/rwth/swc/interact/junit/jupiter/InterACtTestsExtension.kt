package de.rwth.swc.interact.junit.jupiter

import de.rwth.swc.interact.domain.TestInvocationDescriptor
import de.rwth.swc.interact.domain.TestMode
import de.rwth.swc.interact.integrator.Integrator
import de.rwth.swc.interact.junit.jupiter.InterACtTestConstants.ARGUMENT_MAX_LENGTH_KEY
import de.rwth.swc.interact.junit.jupiter.InterACtTestConstants.DEFAULT_DISPLAY_NAME
import de.rwth.swc.interact.junit.jupiter.InterACtTestConstants.DISPLAY_NAME_PATTERN_KEY
import de.rwth.swc.interact.junit.jupiter.InterACtTestConstants.METHOD_CONTEXT_KEY
import de.rwth.swc.interact.junit.jupiter.annotation.InterACtTest
import de.rwth.swc.interact.junit.jupiter.annotation.InterACtTestConstants
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestTemplateInvocationContext
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.junit.jupiter.params.support.AnnotationConsumerInitializer
import org.junit.platform.commons.JUnitException
import org.junit.platform.commons.util.AnnotationUtils.*
import org.junit.platform.commons.util.ExceptionUtils
import org.junit.platform.commons.util.Preconditions
import org.junit.platform.commons.util.ReflectionUtils
import java.lang.reflect.Method
import java.util.concurrent.atomic.AtomicLong
import java.util.stream.Stream


object InterACtTestConstants {
    const val METHOD_CONTEXT_KEY = "context"
    const val ARGUMENT_MAX_LENGTH_KEY = "junit.jupiter.params.displayname.argument.maxlength"
    const val DEFAULT_DISPLAY_NAME = "{default_display_name}"
    const val DISPLAY_NAME_PATTERN_KEY = "junit.jupiter.params.displayname.default"
}

class InterACtTestsExtension : TestTemplateInvocationContextProvider {

    override fun supportsTestTemplate(context: ExtensionContext): Boolean {
        if (!context.testMethod.isPresent) {
            return false
        }

        val testMethod = context.testMethod.get()
        if (!isAnnotated(testMethod, InterACtTest::class.java)) {
            return false
        }

        val methodContext = InterACtTestMethodContext(testMethod)

        Preconditions.condition(
            methodContext.hasPotentiallyValidSignature()
        ) {
            String.format(
                "@InterACtTest method [%s] declares formal parameters in an invalid order: "
                        + "argument aggregators must be declared after any indexed arguments "
                        + "and before any arguments resolved by another ParameterResolver.",
                testMethod.toGenericString()
            )
        }

        getStore(context).put(METHOD_CONTEXT_KEY, methodContext)

        return true
    }

    override fun provideTestTemplateInvocationContexts(context: ExtensionContext): Stream<TestTemplateInvocationContext> {
        Integrator.pullReplacements()
        val templateMethod = context.requiredTestMethod
        val displayName = context.displayName
        val methodContext = getStore(context)
            .get(METHOD_CONTEXT_KEY, InterACtTestMethodContext::class.java)
        val argumentMaxLength: Int = context.getConfigurationParameter(
            ARGUMENT_MAX_LENGTH_KEY,
            Integer::parseInt
        ).orElse(512)
        val formatter = createNameFormatter(
            context, templateMethod, methodContext,
            displayName, argumentMaxLength
        )
        val invocationCount = AtomicLong(0)

        val unitTestStream = findRepeatableAnnotations(templateMethod, ArgumentsSource::class.java)
            .stream()
            .map { obj: ArgumentsSource -> obj.value }
            .map { instantiateArgumentsProvider(it.java) }
            .map { provider -> AnnotationConsumerInitializer.initialize(templateMethod, provider) }
            .flatMap { provider -> arguments(provider, context) }
            .map { it.get() }
            .map { arguments -> consumedArguments(arguments, methodContext) }
            .map { arguments ->
                invocationCount.incrementAndGet()
                createInvocationContext(formatter, methodContext, arguments, invocationCount.toInt(), TestMode.UNIT)
            }
            .onClose {
                Preconditions.condition(
                    invocationCount.get() > 0,
                    "Configuration error: You must configure at least one set of arguments for this @InterACtTest"
                )
            }

        val interactionTestStream = InterACtArgumentsProvider().provideArguments(context)
            .map { arguments ->
                invocationCount.incrementAndGet()
                createInvocationContext(
                    formatter,
                    methodContext,
                    consumedArguments(arguments.second.get(), methodContext),
                    invocationCount.toInt(),
                    TestMode.INTERACTION,
                    arguments.first
                )
            }

        return Stream.concat(unitTestStream, interactionTestStream)
    }

    private fun instantiateArgumentsProvider(clazz: Class<out ArgumentsProvider>): ArgumentsProvider {
        try {
            return ReflectionUtils.newInstance(clazz)
        } catch (ex: Exception) {
            if (ex is NoSuchMethodException) {
                val message = String.format(
                    "Failed to find a no-argument constructor for ArgumentsProvider [%s]. "
                            + "Please ensure that a no-argument constructor exists and "
                            + "that the class is either a top-level class or a static nested class",
                    clazz.name
                )
                throw JUnitException(message, ex)
            }
            throw ex
        }
    }

    private fun getStore(context: ExtensionContext): ExtensionContext.Store {
        return context.getStore(
            ExtensionContext.Namespace.create(
                InterACtTestsExtension::class.java,
                context.requiredTestMethod
            )
        )
    }

    private fun createInvocationContext(
        formatter: InterACtTestNameFormatter,
        methodContext: InterACtTestMethodContext,
        arguments: Array<Any?>,
        invocationIndex: Int,
        mode: TestMode,
        testInvocationDescriptor: TestInvocationDescriptor? = null
    ): TestTemplateInvocationContext {
        return InterACtTestInvocationContext(
            formatter,
            methodContext,
            arguments,
            invocationIndex,
            mode,
            testInvocationDescriptor
        )
    }

    private fun createNameFormatter(
        extensionContext: ExtensionContext,
        templateMethod: Method,
        methodContext: InterACtTestMethodContext,
        displayName: String,
        argumentMaxLength: Int
    ): InterACtTestNameFormatter {
        val parameterizedTest = findAnnotation(templateMethod, InterACtTest::class.java).get()
        var pattern = if (parameterizedTest.name == DEFAULT_DISPLAY_NAME) extensionContext.getConfigurationParameter(
            DISPLAY_NAME_PATTERN_KEY
        ).orElse(
            InterACtTestConstants.DEFAULT_DISPLAY_NAME
        ) else parameterizedTest.name
        pattern = Preconditions.notBlank(pattern.trim { it <= ' ' }
        ) {
            String.format(
                "Configuration error: @ParameterizedTest on method [%s] must be declared with a non-empty name.",
                templateMethod
            )
        }
        return InterACtTestNameFormatter(pattern, displayName, methodContext, argumentMaxLength)
    }

    private fun arguments(provider: ArgumentsProvider, context: ExtensionContext): Stream<out Arguments> {
        return try {
            provider.provideArguments(context)
        } catch (e: java.lang.Exception) {
            throw ExceptionUtils.throwAsUncheckedException(e)
        }
    }

    private fun consumedArguments(arguments: Array<Any?>, methodContext: InterACtTestMethodContext): Array<Any?> {
        if (methodContext.hasAggregator()) {
            return arguments
        }
        val parameterCount: Int = methodContext.getParameterCount()
        return if (arguments.size > parameterCount) arguments.copyOfRange(0, parameterCount) else arguments
    }
}
