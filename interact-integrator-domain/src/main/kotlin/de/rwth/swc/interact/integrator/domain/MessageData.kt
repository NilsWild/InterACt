package de.rwth.swc.interact.integrator.domain

import java.util.*

data class MessageData(
    val protocol: String,
    val protocolData: Map<String, String>,
    val value: String
) {
    var id: UUID? = null
}