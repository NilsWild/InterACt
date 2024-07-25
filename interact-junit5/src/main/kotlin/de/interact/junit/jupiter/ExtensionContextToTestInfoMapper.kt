package de.interact.junit.jupiter

import de.interact.domain.serialization.SerializationConstants
import de.interact.domain.testobservation.model.AbstractTestCaseName
import de.interact.domain.testobservation.model.AbstractTestCaseSource
import de.interact.domain.testobservation.model.TestCaseParameter
import de.interact.test.ComponentInformationLoader
import de.interact.test.PropertiesBasedComponentInformationLoader
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ReflectiveInvocationContext
import java.lang.reflect.Method
import kotlin.reflect.full.createInstance

object ExtensionContextToTestInfoMapper {
    fun componentInformationLoader(context: ExtensionContext): ComponentInformationLoader {
        val c = getContextWithRequiredTestClass(context)
        val annotation =
            c.requiredTestClass.getAnnotation(de.interact.test.annotation.ComponentInformationLoader::class.java)
        return annotation?.value?.createInstance() ?: PropertiesBasedComponentInformationLoader
    }

    fun abstractTestCaseName(context: ExtensionContext): AbstractTestCaseName {
        val c = getContextWithRequiredTestClass(context)
        return AbstractTestCaseName(c.requiredTestMethod.name)
    }

    fun abstractTestCaseSource(context: ExtensionContext): AbstractTestCaseSource {
        val c = getContextWithRequiredTestClass(context)
        return AbstractTestCaseSource(c.requiredTestClass.canonicalName)
    }

    private fun getContextWithRequiredTestClass(context: ExtensionContext): ExtensionContext {
        return if (context.testClass.isPresent) {
            context
        } else {
            getContextWithRequiredTestClass(context.parent.orElseThrow { RuntimeException("Could not retrieve test class!") })
        }
    }

    fun argumentsFrom(context: ReflectiveInvocationContext<Method>): List<TestCaseParameter> {
        val result = mutableListOf<TestCaseParameter>()
        context.arguments.forEachIndexed { index, _ ->
            result.add(getTestCaseParameterFromArgument(context, index))
        }
        return result
    }

    private fun getTestCaseParameterFromArgument(
        context: ReflectiveInvocationContext<Method>,
        index: Int
    ): TestCaseParameter {
        if (context.arguments[index] == null) {
            return TestCaseParameter(null)
        }
        val result = SerializationConstants.mapper.writeValueAsString(context.arguments[index])
        return TestCaseParameter(result)
    }
}