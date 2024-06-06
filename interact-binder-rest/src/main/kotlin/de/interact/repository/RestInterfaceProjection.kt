package de.interact.repository

import de.interact.domain.rest.*
import de.interact.domain.shared.*
import java.util.*

interface RestInterfaceProjection {
    val id: UUID
    val protocolData: Map<String, String>
}

interface IncomingRestInterfaceProjection : RestInterfaceProjection {
    fun toDomain(): IncomingRestInterface = IncomingRestInterface(
        IncomingInterfaceId(id),
         RestInterfaceData(
             protocolData["method"]!!,
             protocolData["url"]!!,
             protocolData["request"] == "true"
         )
    )
}

interface OutgoingRestInterfaceProjection : RestInterfaceProjection {
    fun toDomain(): OutgoingRestInterface = OutgoingRestInterface(
        OutgoingInterfaceId(id),
        RestInterfaceData(
            protocolData["method"]!!,
            protocolData["url"]!!,
            protocolData["request"] == "true"
        )
    )
}

interface RestInterfaceExpectationProjection {
    val id: UUID
    val protocolData: Map<String, String>
}

interface IncomingRestInterfaceExpectationProjection : RestInterfaceExpectationProjection {
    fun toDomain(): IncomingRestInterfaceExpectation = IncomingRestInterfaceExpectation(
        IndirectIncomingInterfaceExpectationId(id),
        RestInterfaceData(
            protocolData["method"]!!,
            protocolData["url"]!!,
            protocolData["request"] == "true"
        )
    )
}

interface OutgoingRestInterfaceExpectationProjection : RestInterfaceExpectationProjection {
    fun toDomain(): OutgoingRestInterfaceExpectation = OutgoingRestInterfaceExpectation(
        IndirectOutgoingInterfaceExpectationId(id),
        RestInterfaceData(
            protocolData["method"]!!,
            protocolData["url"]!!,
            protocolData["request"] == "true"
        )
    )
}