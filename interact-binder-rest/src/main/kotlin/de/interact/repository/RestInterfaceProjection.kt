package de.interact.repository

import de.interact.domain.rest.*
import de.interact.domain.shared.IncomingInterfaceId
import de.interact.domain.shared.IndirectIncomingInterfaceExpectationId
import de.interact.domain.shared.IndirectOutgoingInterfaceExpectationId
import de.interact.domain.shared.OutgoingInterfaceId
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
             protocolData["path"]!!,
             protocolData["request"] == "true"
         )
    )
}

interface OutgoingRestInterfaceProjection : RestInterfaceProjection {
    fun toDomain(): OutgoingRestInterface = OutgoingRestInterface(
        OutgoingInterfaceId(id),
        RestInterfaceData(
            protocolData["method"]!!,
            protocolData["path"]!!,
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
            protocolData["path"]!!,
            protocolData["request"] == "true"
        )
    )
}

interface OutgoingRestInterfaceExpectationProjection : RestInterfaceExpectationProjection {
    fun toDomain(): OutgoingRestInterfaceExpectation = OutgoingRestInterfaceExpectation(
        IndirectOutgoingInterfaceExpectationId(id),
        RestInterfaceData(
            protocolData["method"]!!,
            protocolData["path"]!!,
            protocolData["request"] == "true"
        )
    )
}