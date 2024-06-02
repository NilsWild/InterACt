package de.interact.domain.testobservation.config

import de.interact.domain.testobservation.service.TestObservationManager

data object Configuration {
    var observationManager: TestObservationManager? = null
}
