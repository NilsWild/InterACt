package de.interact.junit.jupiter

import de.interact.domain.testobservation.config.Configuration
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestPlan

/**
 * The InterACt junit extension is used to store the recorded observed messages
 */
class StoreObservationsAfterTestExecution : TestExecutionListener {

    override fun testPlanExecutionFinished(testPlan: TestPlan) {
        Configuration.observationManager?.publishObservation()
    }
}