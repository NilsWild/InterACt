package de.rwth.swc.interact.junit.jupiter.systemexpectations

import de.rwth.swc.interact.domain.Component
import de.rwth.swc.interact.domain.SystemExpectationCandidateId
import de.rwth.swc.interact.domain.SystemPropertyExpectation
import de.rwth.swc.interact.domain.serialization.SerializationConstants
import de.rwth.swc.interact.junit.jupiter.ParameterTypeResolution
import de.rwth.swc.interact.observer.ObservationControllerApi
import de.rwth.swc.interact.observer.TestObserver
import de.rwth.swc.interact.test.PropertiesBasedComponentInformationLoader
import de.rwth.swc.interact.test.annotation.ComponentInformationLoader
import de.rwth.swc.interact.test.systemexpectations.SystemPropertyAssertionError
import de.rwth.swc.interact.test.systemexpectations.SystemPropertyMessageFilterError
import io.vertx.core.Vertx
import org.junit.jupiter.api.extension.*
import java.lang.reflect.Method
import java.util.*
import kotlin.reflect.full.createInstance

class SystemExpectationInvocationContext(
    private val systemPropertyExpectation: SystemPropertyExpectation,
    private val candidateId: SystemExpectationCandidateId?,
    private val phase: SystemExpectationPhase
) : TestTemplateInvocationContext {
    override fun getAdditionalExtensions(): List<Extension> {
        var stimulus: String? = null
        var response: String? = null
        candidateId?.let {
            systemPropertyExpectation.systemExpectationCandidates.find { it.id == candidateId }?.let {
                stimulus = it.from.value.value
                response = it.to.value.value
            } ?: throw IllegalArgumentException("Candidate with id $candidateId not found in system expectation")
        }

        return listOf(
            StimulusAndResponseParameterResolver(systemPropertyExpectation, stimulus, response),
            SystemExpectationExtension(systemPropertyExpectation, phase)
        )
    }
}

class StimulusAndResponseParameterResolver(private val expectation: SystemPropertyExpectation, private val stimulus: String?, private val response: String?) :
    ParameterResolver {

    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        return true
    }

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any? {
        return when (parameterContext.index) {
            0 -> expectation
            1 -> ParameterTypeResolution.resolveArgumentToParameterType(parameterContext, stimulus)
            2 -> ParameterTypeResolution.resolveArgumentToParameterType(parameterContext, response)
            else -> null
        }
    }
}

class SystemExpectationExtension(
    private val systemExpectation: SystemPropertyExpectation,
    private val phase: SystemExpectationPhase
) : InvocationInterceptor {

    override fun interceptTestTemplateMethod(
        invocation: InvocationInterceptor.Invocation<Void>,
        invocationContext: ReflectiveInvocationContext<Method>,
        extensionContext: ExtensionContext
    ) {
        try {
            super.interceptTestTemplateMethod(invocation, invocationContext, extensionContext)
        } catch (e: SystemPropertyMessageFilterError) {
            if (phase == SystemExpectationPhase.STIMULUS_FILTERING || phase == SystemExpectationPhase.ASSERTION) {
                throw e
            }
        } catch (e: SystemPropertyAssertionError) {
            if (phase == SystemExpectationPhase.ASSERTION) {
                throw e
            }
        } catch (e: Throwable) {
            throw e
        }
        if(phase == SystemExpectationPhase.DISCOVERY) {
            val c = getContextWithRequiredTestClass(extensionContext)
            val annotation = c.requiredTestClass.getAnnotation(ComponentInformationLoader::class.java)
            val componentInformationLoader = annotation?.value?.createInstance() ?: PropertiesBasedComponentInformationLoader
            val component = Component(
                componentInformationLoader.getComponentName(),
                componentInformationLoader.getComponentVersion()
            )
            component.systemPropertyExpectations.add(systemExpectation)
            val props = Properties()
            props.load(this.javaClass.classLoader.getResourceAsStream("interact.properties"))
            val client = SystemPropertyExpectationControllerApi(
                props.getProperty("broker.url", "http://localhost:8080"),
                SerializationConstants.mapper,
                Vertx.vertx()
            )
            client.storeSystemPropertyExpectation(component).toCompletionStage().toCompletableFuture().join()
        }
    }

    private fun getContextWithRequiredTestClass(context: ExtensionContext): ExtensionContext {
        return if (context.testClass.isPresent) {
            context
        } else {
            getContextWithRequiredTestClass(context.parent.orElseThrow { RuntimeException("Could not retrieve test class!") })
        }
    }

}
