package de.rwth.swc.interact.controller.integrations.dto

import de.rwth.swc.interact.domain.ConcreteTestCaseId
import de.rwth.swc.interact.domain.InterfaceId
import de.rwth.swc.interact.domain.MessageId

data class InteractionTestInfo(
    val testCaseId: ConcreteTestCaseId,
    val nextStart: MessageId?,
    val replacements: Map<MessageId, InterfaceId>
)