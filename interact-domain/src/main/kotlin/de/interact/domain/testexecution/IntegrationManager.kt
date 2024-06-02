package de.interact.domain.testexecution

import de.interact.domain.testexecution.api.Integrations
import de.interact.domain.testexecution.spi.TestExecutionRequests

class IntegrationManager(
    private val testExecutionRequests: TestExecutionRequests
) : Integrations {

    override fun findInteractionTestsToExecuteForComponent(
        name: String,
        version: String
    ): List<TestInvocationDescriptor> {
        return testExecutionRequests.findInteractionTestsToExecuteForComponent(name, version)
    }
}