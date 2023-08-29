package de.rwth.swc.interact.junit.jupiter

import de.rwth.swc.interact.domain.AbstractTestCaseName
import de.rwth.swc.interact.domain.ConcreteTestCaseName
import de.rwth.swc.interact.domain.TestCaseParameter
import de.rwth.swc.interact.domain.TestMode
import de.rwth.swc.interact.domain.serialization.SerializationConstants
import de.rwth.swc.interact.observer.TestObserver
import de.rwth.swc.interact.test.ExampleBasedAssertionError
import de.rwth.swc.interact.test.annotation.ComponentInformationLoader
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.InvocationInterceptor
import org.junit.jupiter.api.extension.ReflectiveInvocationContext
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import kotlin.reflect.full.createInstance

class InterACtTestInvocationInterceptor(
    private val mode: TestMode
) : InvocationInterceptor {

    override fun interceptTestTemplateMethod(
        invocation: InvocationInterceptor.Invocation<Void>,
        invocationContext: ReflectiveInvocationContext<Method>,
        extensionContext: ExtensionContext
    ) {
        observeTest(extensionContext, argumentsFrom(invocationContext), mode)
        try {
            try {
                super.interceptTestTemplateMethod(invocation, invocationContext, extensionContext)
            } catch (e: ExampleBasedAssertionError) {
                if (mode == TestMode.UNIT) {
                    throw e
                }
            }
        } catch (e: Throwable) {
            if (mode == TestMode.UNIT) {
                TestObserver.dropObservation()
                throw e
            }
        }
    }

    private fun observeTest(context: ExtensionContext, arguments: List<TestCaseParameter?>, mode: TestMode) {
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

    private fun argumentsFrom(context: ReflectiveInvocationContext<Method>): List<TestCaseParameter?> {
        //TODO when https://github.com/ProjectMapK/jackson-module-kogera/issues/42 is resolved, TestCaseParameter should wrap nullable
        val result = mutableListOf<TestCaseParameter?>()
        context.arguments.forEachIndexed { index, _ ->
            result.add(getTestCaseParameterFromArgument(context, index))
        }
        return result
    }

    private fun getTestCaseParameterFromArgument(
        context: ReflectiveInvocationContext<Method>,
        index: Int
    ): TestCaseParameter? {
        if (context.arguments[index] == null) {
            return null
        }
        val type = context.executable.genericParameterTypes[index]
        val result = if (type is ParameterizedType) {
            val genericTypes =
                type.actualTypeArguments.map { SerializationConstants.mapper.typeFactory.constructType(it) }
                    .toTypedArray()
            SerializationConstants.mapper.writerFor(
                SerializationConstants.mapper.typeFactory.constructParametricType(
                    context.executable.parameterTypes[index],
                    *genericTypes
                )
            )
                .writeValueAsString(
                    context.arguments[index]
                )
        } else {
            SerializationConstants.mapper.writeValueAsString(context.arguments[index])
        }
        return TestCaseParameter(result)
    }
}