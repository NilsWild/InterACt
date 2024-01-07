package de.rwth.swc.interact.junit.jupiter.systemexpectations

import de.rwth.swc.interact.domain.SystemPropertyExpectation
import de.rwth.swc.interact.domain.SystemPropertyExpectationName
import de.rwth.swc.interact.domain.SystemPropertyExpectationSource
import de.rwth.swc.interact.junit.jupiter.InterACtTestConstants
import de.rwth.swc.interact.junit.jupiter.InterACtTestMethodContext
import de.rwth.swc.interact.junit.jupiter.InterACtTestNameFormatter
import de.rwth.swc.interact.junit.jupiter.systemexpectations.annotation.SystemExpectation
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestTemplateInvocationContext
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider
import org.junit.platform.commons.util.AnnotationUtils
import org.junit.platform.commons.util.Preconditions
import java.lang.reflect.Method
import java.util.stream.Stream

class SystemPropertyTestExtension: TestTemplateInvocationContextProvider {

    override fun supportsTestTemplate(context: ExtensionContext): Boolean {
        if (!context.testMethod.isPresent) {
            return false
        }

        val testMethod = context.testMethod.get()
        if (!AnnotationUtils.isAnnotated(testMethod, SystemExpectation::class.java)) {
            return false
        }

        val methodContext = SystemPropertyTestMethodContext(testMethod)

        Preconditions.condition(
            methodContext.hasPotentiallyValidSignature()
        ) {
            String.format(
                "@SystemExpectation method [%s] declares formal parameters in an invalid order: " +
                        "the first arument must be a SystemPropertyExpectation, " +
                        "the second argument must be a stimulus message and the third " +
                        "argument must be a response message.",
                testMethod.toGenericString()
            )
        }

        getStore(context).put(InterACtTestConstants.METHOD_CONTEXT_KEY, methodContext)

        return true
    }

    private fun getStore(context: ExtensionContext): ExtensionContext.Store {
        return context.getStore(
            ExtensionContext.Namespace.create(
                SystemPropertyTestExtension::class.java,
                context.requiredTestMethod
            )
        )
    }

    override fun provideTestTemplateInvocationContexts(context: ExtensionContext): Stream<TestTemplateInvocationContext> {
        val templateMethod = context.requiredTestMethod
        val displayName = context.displayName
        val methodContext = getStore(context)
            .get(InterACtTestConstants.METHOD_CONTEXT_KEY, SystemPropertyTestMethodContext::class.java)
        val argumentMaxLength: Int = context.getConfigurationParameter(
            InterACtTestConstants.ARGUMENT_MAX_LENGTH_KEY,
            Integer::parseInt
        ).orElse(512)
        val formatter = createNameFormatter(
            context, templateMethod, methodContext,
            displayName, argumentMaxLength
        )
        val sourceAndName = getClassSourceAndMethodName(context)
        val systemPropertyExpectation = SystemPropertyExpectation(sourceAndName.first,sourceAndName.second)
        val systemPropertyDiscoveryContext = SystemExpectationInvocationContext(
            systemPropertyExpectation,
            null,
            SystemExpectationPhase.DISCOVERY
        )
        //proceed with evaluation of system expectation candidates
        return Stream.of(systemPropertyDiscoveryContext)
    }

    private fun getClassSourceAndMethodName(context: ExtensionContext): Pair<SystemPropertyExpectationSource, SystemPropertyExpectationName> {
        return if (context.testClass.isPresent) {
            Pair(
                SystemPropertyExpectationSource(context.requiredTestClass.canonicalName),
                SystemPropertyExpectationName(context.requiredTestMethod.name)
            )
        } else {
            getClassSourceAndMethodName(context.parent.orElseThrow { RuntimeException("Could not retrieve test class!") })
        }
    }


    private fun createNameFormatter(
        extensionContext: ExtensionContext,
        templateMethod: Method,
        methodContext: SystemPropertyTestMethodContext,
        displayName: String,
        argumentMaxLength: Int
    ): SystemPropertyTestNameFormatter {
        val parameterizedTest = AnnotationUtils.findAnnotation(templateMethod, SystemExpectation::class.java).get()
        var pattern = if (parameterizedTest.name == InterACtTestConstants.DEFAULT_DISPLAY_NAME) extensionContext.getConfigurationParameter(
            InterACtTestConstants.DISPLAY_NAME_PATTERN_KEY
        ).orElse(
            InterACtTestConstants.DEFAULT_DISPLAY_NAME
        ) else parameterizedTest.name
        pattern = Preconditions.notBlank(pattern.trim { it <= ' ' }
        ) {
            String.format(
                "Configuration error: @SystemExpectation on method [%s] must be declared with a non-empty name.",
                templateMethod
            )
        }
        return SystemPropertyTestNameFormatter(pattern, displayName, methodContext, argumentMaxLength)
    }

}