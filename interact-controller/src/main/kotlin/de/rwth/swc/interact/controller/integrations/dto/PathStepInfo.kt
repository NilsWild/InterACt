package de.rwth.swc.interact.controller.integrations.dto

import java.util.*

data class PathStepInfo(
    val next: UUID,
    val originalMessage: UUID,
    val replacementMessageInterface: UUID,
    val component: UUID,
    val testCase: UUID,
    val originalMessageInterface: UUID
)