package de.rwth.swc.interact.domain

data class TestInvocationDescriptor (
    val interactionExpectationId: InteractionExpectationId,
    val messages: List<MessageValue>
)