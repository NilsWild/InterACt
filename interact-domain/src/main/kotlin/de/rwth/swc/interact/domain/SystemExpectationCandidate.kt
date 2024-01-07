package de.rwth.swc.interact.domain

import java.util.UUID

data class SystemExpectationCandidate(
    val from: Message,
    val to: Message
) {
    var id: SystemExpectationCandidateId? = null
    var selected: Boolean? = null
    var validated: Boolean? = null
}

@JvmInline
value class SystemExpectationCandidateId(val id: UUID) {
    override fun toString() = id.toString()

    companion object {
        fun random() = SystemExpectationCandidateId(UUID.randomUUID())
    }
}