package de.rwth.swc.interact.controller.integrations.dto

import java.util.*

data class InteractionPathInfo(
    val interactionTests: List<InteractionTestInfo>,
    val visitedInterfaces: List<UUID> = listOf(),
    val visitedComponentTestCase: Map<UUID, UUID> = mapOf(),
    val testCaseReplacements: Map<UUID, Map<UUID, UUID>> = mapOf()
)