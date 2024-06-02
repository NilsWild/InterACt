package de.interact.domain.testexecution.api

import de.interact.domain.testexecution.TestInvocationDescriptor

interface Integrations {

    fun findInteractionTestsToExecuteForComponent(name: String, version: String): List<TestInvocationDescriptor>
}