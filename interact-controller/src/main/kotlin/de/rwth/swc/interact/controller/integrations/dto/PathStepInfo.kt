package de.rwth.swc.interact.controller.integrations.dto

import de.rwth.swc.interact.domain.*

data class PathStepInfo(
    val next: MessageId,
    val originalMessage: MessageId,
    val replacementMessageInterface: InterfaceId,
    val component: ComponentId,
    val testCase: ConcreteTestCaseId,
    val originalMessageInterface: InterfaceId
)