package de.interact.domain.testobservation.spi

import de.interact.domain.testobservation.model.TestObservation

interface ObservationPublisher {
    fun publish(observation: TestObservation): Boolean
}