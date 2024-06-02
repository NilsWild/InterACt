package de.interact.domain.testobservation.service

import de.interact.domain.testobservation.model.ConcreteTestCase
import de.interact.domain.testobservation.model.TestObservation
import de.interact.domain.testobservation.spi.MessageObserver
import de.interact.domain.testobservation.spi.ObservationPublisher
import de.interact.domain.testobservation.spi.TestObservationContextManager

class TestObservationManager(
    val messageObservers: MutableList<MessageObserver>,
    private val testObservationContextManager: TestObservationContextManager,
    private val observationPublisher: ObservationPublisher
) {
    val observation = TestObservation()

    fun observeTestCase(testCase: ConcreteTestCase, testMethod: () -> Unit) {
        require(observation.observedComponents.flatMap { it.testedBy }.flatMap { it.templateFor }.contains(testCase))
        testObservationContextManager.startObservationContext(testCase, testMethod)
    }

    fun getCurrentTestCase(): ConcreteTestCase {
        return testObservationContextManager.getCurrentTestCase(observation)
    }

    fun testFinished(): Boolean {
        return messageObservers.all { it.isFinished() }
    }

    fun publishObservation() {
        if (testFinished()) {
            observationPublisher.publish(observation)
        } else {
            throw IllegalStateException("Not all message observers are finished")
        }
    }
}