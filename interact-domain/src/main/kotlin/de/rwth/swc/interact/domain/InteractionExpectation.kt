package de.rwth.swc.interact.domain

import java.util.UUID

data class InteractionExpectation (
    val from: SentMessage,
    val to: ReceivedMessage,
    val validated: Boolean = false
) {
    var id: InteractionExpectationId? = null
}

@JvmInline
value class InteractionExpectationId(val id: UUID) {
    override fun toString() = id.toString()

    companion object {
        fun random() = InteractionExpectationId(UUID.randomUUID())
    }
}