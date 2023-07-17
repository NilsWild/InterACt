package de.rwth.swc.interact.domain

data class TestInvocationDescriptor (
    val interactionExpectationId: InteractionExpectationId,
    val replacements: Map<ReceivedMessage, SentMessage> = mapOf()
)