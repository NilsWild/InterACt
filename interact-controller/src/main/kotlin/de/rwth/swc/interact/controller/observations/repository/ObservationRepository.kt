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
        var query = ""
        protocolData.data.forEach { (k, v) ->
            query += "AND ii.`protocolData.$k`=\"$v\" "
        }
        return neo4jClient.query(
            "MATCH (c:Component)-[:PROVIDES]->(ii:IncomingInterface) " +
                    "WHERE c.id=\$componentId AND ii.protocol=\$protocol $query " +
                    "RETURN ii.id as id"
        ).bind(componentId.toString()).to("componentId")
            .bind(protocol.toString()).to("protocol")
            .fetchAs(InterfaceId::class.java).mappedBy { _, record ->
                InterfaceId(UUID.fromString(record.get("id").asString()))
            }.first().orElse(null)
    }

    fun findOutgoingInterfaceIdByComponentIdAndProtocolAndName(
        componentId: ComponentId,
        protocol: Protocol,
        protocolData: ProtocolData
    ): InterfaceId? {
        var query = ""
        protocolData.data.forEach { (k, v) ->
            query += "AND oi.`protocolData.$k`=\"$v\" "
        }
        return neo4jClient.query(
            "MATCH (c:Component)-[:REQUIRES]->(oi:OutgoingInterface) " +
                    "WHERE c.id=\$componentId AND oi.protocol=\$protocol $query " +
                    "RETURN oi.id as id"
        ).bind(componentId.toString()).to("componentId")
            .bind(protocol.toString()).to("protocol")
            .fetchAs(InterfaceId::class.java).mappedBy { _, record ->
                InterfaceId(UUID.fromString(record.get("id").asString()))
            }.first().orElse(null)
    }
}