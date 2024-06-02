package de.interact.junit.jupiter.systemexpectations

import de.interact.domain.specification.config.Configuration
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestPlan

/**
 * The InterACt junit extension is used to store the defined expectations
 */
class StoreExpectationsAfterTestExecution : TestExecutionListener {

    override fun testPlanExecutionFinished(testPlan: TestPlan) {
        Configuration.expectationsManager?.publishExpectations()
    }
}