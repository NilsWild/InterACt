package de.rwth.swc.interact.controller.integrations.dto

import de.rwth.swc.interact.domain.InteractionExpectationId
import de.rwth.swc.interact.domain.MessageId

data class InteractionExpectationInfo(
    val id: InteractionExpectationId,
    val fromId: MessageId,
    val toIds: List<MessageId>,
)