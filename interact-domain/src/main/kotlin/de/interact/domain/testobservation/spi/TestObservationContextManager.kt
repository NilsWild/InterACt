package de.interact.domain.testobservation.spi

import de.interact.domain.testobservation.model.ConcreteTestCase
import de.interact.domain.testobservation.model.TestObservation

interface TestObservationContextManager {

    fun getCurrentTestCase(observation: TestObservation): ConcreteTestCase
    fun startObservationContext(testCase: ConcreteTestCase, testMethod: () -> Unit)

}