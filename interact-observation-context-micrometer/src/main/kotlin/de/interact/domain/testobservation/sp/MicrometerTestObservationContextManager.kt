package de.interact.domain.testobservation.sp

import de.interact.domain.testobservation.model.ConcreteTestCase
import de.interact.domain.testobservation.model.TestObservation
import de.interact.domain.testobservation.spi.TestObservationContextManager
import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry

class MicrometerTestObservationContextManager(private val registry: ObservationRegistry) :
    TestObservationContextManager {
    override fun getCurrentTestCase(observation: TestObservation): ConcreteTestCase {
        val testContext = findContextStartingWith(
            registry.currentObservation!!.contextView,
            "InterACt.observation"
        ).name.replace("InterACt.observation:", "")
        return observation.observedComponents.flatMap { it.testedBy }.flatMap { it.templateFor }.first {
            testIdentifier(it) == testContext
        }
    }

    override fun startObservationContext(testCase: ConcreteTestCase, testMethod: () -> Unit) {
        Observation.createNotStarted("InterACt.observation:" + testIdentifier(testCase), registry).observe {
            testMethod()
        }
    }

    private fun testIdentifier(testCase: ConcreteTestCase): String {
        return "${testCase.derivedFrom.component.name}:" +
                "${testCase.derivedFrom.component.version}:" +
                "${testCase.derivedFrom.source}:" +
                "${testCase.derivedFrom.name}:" +
                "${testCase.name}:[" +
                testCase.parameters.joinToString(",") +
                "]"
    }

    private fun findContextStartingWith(context: Observation.ContextView, start: String): Observation.ContextView {
        if (context.name.startsWith(start)) {
            return context
        }
        return context.parentObservation?.contextView?.let {
            findContextStartingWith(it, start)
        } ?: throw RuntimeException("No context found starting with $start")

    }
}