package de.interact.domain.testobservation.sp

import de.interact.domain.testobservation.model.ConcreteTestCase
import de.interact.domain.testobservation.model.TestObservation
import de.interact.domain.testobservation.spi.TestObservationContextManager

class SimpleTestObservationContextManager : TestObservationContextManager {
    override fun getCurrentTestCase(observation: TestObservation): ConcreteTestCase {
        return observation.observedComponents.flatMap { it.testedBy }.flatMap { it.templateFor }.last()
    }

    override fun startObservationContext(testCase: ConcreteTestCase, testMethod: () -> Unit) {
        testMethod()
    }
}