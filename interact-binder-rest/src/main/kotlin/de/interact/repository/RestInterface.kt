package de.interact.repository

import java.util.*

interface RestInterface {
    val id: UUID
    val protocolData: Map<String, String>
}

interface IncomingRestInterface : RestInterface {
    fun restProtocolData(): IncomingRestInterfaceProtocolData = IncomingRestInterfaceProtocolData(
        protocolData["request"] == "true",
        protocolData["method"]!!,
        protocolData["url"]!!
    )
}

interface OutgoingRestInterface : RestInterface {
    fun restProtocolData(): OutgoingRestInterfaceProtocolData = OutgoingRestInterfaceProtocolData(
        protocolData["request"] == "true",
        protocolData["method"]!!,
        protocolData["url"]!!
    )
}

interface RestInterfaceExpectation {
    val id: UUID
    val protocolData: Map<String, String>
}

interface IncomingRestInterfaceExpectation : RestInterfaceExpectation {
    fun restProtocolData(): OutgoingRestInterfaceProtocolData = OutgoingRestInterfaceProtocolData(
        protocolData["request"] == "true",
        protocolData["method"]!!,
        protocolData["path"]!!
    )
}

interface OutgoingRestInterfaceExpectation : RestInterfaceExpectation {
    fun restProtocolData(): IncomingRestInterfaceProtocolData = IncomingRestInterfaceProtocolData(
        protocolData["request"] == "true",
        protocolData["method"]!!,
        protocolData["path"]!!
    )
}

sealed interface RestInterfaceProtocolData {
    val request: Boolean
    val method: String
    val path: String
}

data class IncomingRestInterfaceProtocolData(
    override val request: Boolean,
    override val method: String,
    override val path: String
) : RestInterfaceProtocolData {

}

data class OutgoingRestInterfaceProtocolData(
    override val request: Boolean,
    override val method: String,
    override val path: String
) : RestInterfaceProtocolData {

}