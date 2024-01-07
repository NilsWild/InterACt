package de.rwth.swc.interact.controller.observations.repository

import de.rwth.swc.interact.domain.ComponentId
import de.rwth.swc.interact.domain.InterfaceId
import de.rwth.swc.interact.domain.Protocol
import de.rwth.swc.interact.domain.ProtocolData
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Component
import java.util.*

@Component
class ObservationRepository(
    private val neo4jClient: Neo4jClient
) {

    fun findIncomingInterfaceIdByComponentIdAndProtocolAndName(
        componentId: ComponentId,
        protocol: Protocol,
        protocolData: ProtocolData
    ): InterfaceId? {
        return neo4jClient.query(
            "MATCH (c:Component)-[:PROVIDES]->(ii:IncomingInterface) " +
                    "WHERE c.id=\$componentId AND ii.protocol=\$protocol AND ii.protocolData=\$protocolData " +
                    "RETURN ii.id as id"
        ).bind(componentId.toString()).to("componentId")
            .bind(protocol.toString()).to("protocol")
            .bind(protocolData.data).to("protocolData")
            .fetchAs(InterfaceId::class.java).mappedBy { _, record ->
                InterfaceId(UUID.fromString(record.get("id").asString()))
            }.first().orElse(null)
    }

    fun findOutgoingInterfaceIdByComponentIdAndProtocolAndName(
        componentId: ComponentId,
        protocol: Protocol,
        protocolData: ProtocolData
    ): InterfaceId? {
        return neo4jClient.query(
            "MATCH (c:Component)-[:REQUIRES]->(oi:OutgoingInterface) " +
                    "WHERE c.id=\$componentId AND oi.protocol=\$protocol AND oi.protocolData=\$protocolData " +
                    "RETURN oi.id as id"
        ).bind(componentId.toString()).to("componentId")
            .bind(protocol.toString()).to("protocol")
            .bind(protocolData.data).to("protocolData")
            .fetchAs(InterfaceId::class.java).mappedBy { _, record ->
                InterfaceId(UUID.fromString(record.get("id").asString()))
            }.first().orElse(null)
    }
}