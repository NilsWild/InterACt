package de.rwth.swc.interact.controller.integrations.dto

data class InteractionExpectationValidationStatus(
    val interactionPathInfo: InteractionPathInfo,
    val interactionPathQueue: List<InteractionPathInfo>
)