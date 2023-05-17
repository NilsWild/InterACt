package de.rwth.swc.interact.observer.domain

import java.util.*

data class ObservedMessage(
    val protocol: String,
    val protocolData: Map<String, String>,
    val type: Type,
    val value: String,
    val isParameter: Boolean,
    val originalMessageId: UUID? = null
) {

    enum class Type {
        STIMULUS,
        COMPONENT_RESPONSE,
        ENVIRONMENT_RESPONSE
    }
}

