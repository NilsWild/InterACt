package de.rwth.swc.interact.controller.integrations.dto

import java.util.*

data class InteractionExpectationInfo(
    val id: UUID,
    val fromId: UUID,
    val toId: UUID,
    val baseTest: UUID,
    val queue: List<InteractionPathInfo>
)