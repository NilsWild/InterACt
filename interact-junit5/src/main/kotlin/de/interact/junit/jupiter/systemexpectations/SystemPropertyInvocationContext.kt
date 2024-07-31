package de.interact.junit.jupiter.systemexpectations

import de.interact.domain.expectations.execution.result.SystemPropertyExpectationRecord
import de.interact.domain.expectations.execution.result.SystemPropertyExpectationsCollectionReference
import de.interact.domain.expectations.execution.systemexpectation.SystemExpectationBuilder
import de.interact.domain.shared.SystemPropertyExpectationIdentifier
import de.interact.domain.specification.config.Configuration
import de.interact.domain.testobservation.model.AbstractTestCaseName
import de.interact.domain.testobservation.model.AbstractTestCaseSource
import org.junit.jupiter.api.extension.*
import java.lang.reflect.Method

class SystemPropertyInvocationContext(
    private val collection: SystemPropertyExpectationsCollectionReference,
    private val sourceAndName: Pair<AbstractTestCaseSource, AbstractTestCaseName>
): TestTemplateInvocationContext {
    override fun getAdditionalExtensions(): List<Extension> {
        return listOf(
            SystemPropertyParameterResolver(SystemExpectationBuilder.SystemPropertyExpectationBuilder(
                SystemPropertyExpectationIdentifier("${sourceAndName.first}:${sourceAndName.second}")
            )),
            SystemPropertyExtension(collection, sourceAndName)
        )
    }
}

class SystemPropertyParameterResolver(private val systemPropertyExpectationBuilder: SystemExpectationBuilder.SystemPropertyExpectationBuilder) : ParameterResolver {
    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        return true
    }

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any? {
        return when (parameterContext.index) {
            0 -> systemPropertyExpectationBuilder
            else -> null
        }
    }
}

class SystemPropertyExtension(
    private val collection: SystemPropertyExpectationsCollectionReference,
    private val sourceAndName: Pair<AbstractTestCaseSource, AbstractTestCaseName>
) : InvocationInterceptor {
    override fun interceptTestTemplateMethod(
        invocation: InvocationInterceptor.Invocation<Void>,
        invocationContext: ReflectiveInvocationContext<Method>,
        extensionContext: ExtensionContext
    ) {
        try {
            super.interceptTestTemplateMethod(invocation, invocationContext, extensionContext)
            val property = (invocationContext.arguments[0] as SystemExpectationBuilder.SystemPropertyExpectationBuilder).build()
            Configuration.expectationsManager!!.addSystemProperty(
                collection,
                SystemPropertyExpectationRecord(
                    property.identifier,
                    property.stimulusInterfaceExpectation,
                    property.reactionInterfaceExpectation
                )
            )
        } catch (e: Exception) {
            throw e
        }
    }
}