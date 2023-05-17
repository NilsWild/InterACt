package de.rwth.swc.interact.controller.integrations.dto

import java.util.*

data class InteractionTestInfo(
    val testCaseId: UUID,
    val nextStart: UUID?,
    val replacements: Map<UUID, UUID>
)