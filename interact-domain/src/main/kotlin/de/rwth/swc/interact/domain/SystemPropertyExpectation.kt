package de.rwth.swc.interact.domain

import java.util.*

data class SystemPropertyExpectation(
    val source: SystemPropertyExpectationSource,
    val name: SystemPropertyExpectationName,
) {
    var id: SystemPropertyExpectationId? = null
    var fromInterface: OutgoingInterfaceExpectation? = null
    var toInterface: IncomingInterfaceExpectation? = null
    var systemExpectationCandidates = mutableListOf<SystemExpectationCandidate>()
    var validated: Boolean? = null
}

@JvmInline
value class SystemPropertyExpectationId(val id: UUID) {
    override fun toString() = id.toString()

    companion object {
        fun random() = SystemPropertyExpectationId(UUID.randomUUID())
    }
}

@JvmInline
value class SystemPropertyExpectationSource(val source: String) {
    override fun toString() = source
}

@JvmInline
value class SystemPropertyExpectationName(val name: String) {
    override fun toString() = name
}