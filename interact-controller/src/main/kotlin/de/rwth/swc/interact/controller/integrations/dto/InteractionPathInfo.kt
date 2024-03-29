package de.rwth.swc.interact.controller.integrations.dto

import de.rwth.swc.interact.domain.ComponentId
import de.rwth.swc.interact.domain.ConcreteTestCaseId
import de.rwth.swc.interact.domain.InterfaceId
import de.rwth.swc.interact.domain.MessageId

data class InteractionPathInfo(
    val interactionTests: List<InteractionTestInfo>,
    val visitedInterfaces: List<InterfaceId> = listOf(),
    val visitedComponentTestCase: Map<ComponentId, ConcreteTestCaseId> = mapOf(),
    val testCaseReplacements: Map<ConcreteTestCaseId, Map<MessageId, InterfaceId>> = mapOf()
)