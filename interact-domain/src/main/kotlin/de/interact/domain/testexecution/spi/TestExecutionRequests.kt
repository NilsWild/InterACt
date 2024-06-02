package de.interact.domain.testexecution.spi

import de.interact.domain.testexecution.TestInvocationDescriptor

interface TestExecutionRequests {
    fun findInteractionTestsToExecuteForComponent(name: String, version: String): List<TestInvocationDescriptor>
}