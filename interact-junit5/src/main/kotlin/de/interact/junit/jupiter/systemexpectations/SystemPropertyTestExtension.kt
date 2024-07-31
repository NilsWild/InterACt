package de.interact.junit.jupiter.systemexpectations

import de.interact.domain.expectations.execution.result.SystemPropertyExpectationsCollectionReference
import de.interact.domain.expectations.specification.collection.ExpectationsCollectionName
import de.interact.domain.expectations.specification.collection.ExpectationsCollectionVersion
import de.interact.domain.testobservation.model.AbstractTestCaseName
import de.interact.domain.testobservation.model.AbstractTestCaseSource
import de.interact.junit.jupiter.ExtensionContextToTestInfoMapper
import de.interact.junit.jupiter.InterACtTestConstants
import de.interact.junit.jupiter.systemexpectations.annotation.SystemPropertyExpectation
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestTemplateInvocationContext
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider
import org.junit.platform.commons.util.AnnotationUtils
import org.junit.platform.commons.util.Preconditions
import java.lang.reflect.Method
import java.util.stream.Stream

class SystemPropertyTestExtension : TestTemplateInvocationContextProvider {

    override fun supportsTestTemplate(context: ExtensionContext): Boolean {
        if (!context.testMethod.isPresent) {
            return false
        }

        val testMethod = context.testMethod.get()
        if (!AnnotationUtils.isAnnotated(testMethod, SystemPropertyExpectation::class.java)) {
            return false
        }

        val methodContext = SystemPropertyTestMethodContext(testMethod)

        Preconditions.condition(
            methodContext.hasPotentiallyValidSignature()
        ) {
            String.format(
                "@SystemPropertyExpectation method [%s] declares formal parameters in an invalid order: " +
                        "the first arument must be a SystemPropertyExpectationBuilder, " +
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

        val compInfoLoader = ExtensionContextToTestInfoMapper.componentInformationLoader(context)
        val expectationsCollection = SystemPropertyExpectationsCollectionReference(
            ExpectationsCollectionName(compInfoLoader.getComponentName().value),
            ExpectationsCollectionVersion(compInfoLoader.getComponentVersion().value)
        )

        val systemPropertyInvocationContext = SystemPropertyInvocationContext(expectationsCollection, sourceAndName)
        //proceed with evaluation of system expectation candidates
        return Stream.of(systemPropertyInvocationContext)
    }

    private fun getClassSourceAndMethodName(context: ExtensionContext): Pair<AbstractTestCaseSource, AbstractTestCaseName> {
        return if (context.testClass.isPresent) {
            Pair(
                AbstractTestCaseSource(context.requiredTestClass.canonicalName),
                AbstractTestCaseName(context.requiredTestMethod.name)
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
        val parameterizedTest =
            AnnotationUtils.findAnnotation(templateMethod, SystemPropertyExpectation::class.java).get()
        var pattern =
            if (parameterizedTest.name == InterACtTestConstants.DEFAULT_DISPLAY_NAME) extensionContext.getConfigurationParameter(
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