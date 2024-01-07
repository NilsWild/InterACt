package de.rwth.swc.interact.utbi

import de.rwth.swc.interact.controller.persistence.domain.INCOMING_INTERFACE_NODE_LABEL
import de.rwth.swc.interact.controller.persistence.domain.OUTGOING_INTERFACE_NODE_LABEL
import de.rwth.swc.interact.domain.*
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
class AmqpInterfaceRepository(private val neo4jClient: Neo4jClient) {
    fun findAllOutgoingInterfaces() = neo4jClient.query(
        "MATCH (o:$OUTGOING_INTERFACE_NODE_LABEL {protocol: \"AMQP\"}) " +
                "RETURN o.id as id, o.`protocolData.exchangeType` as exchangeType, o.`protocolData.exchangeName` as exchangeName, o.`protocolData.routingKey` as routingKey, o.`protocolData.headers` as headers"
    )
        .fetchAs(OutgoingInterface::class.java)
        .mappedBy { _, record ->
            OutgoingInterface(
                Protocol("AMQP"),
                ProtocolData(
                    mapOf(
                        Pair("exchangeType", record.get("exchangeType").asString()),
                        Pair("exchangeName", record.get("exchangeName").asString()),
                        Pair("routingKey", record.get("routingKey").asString()),
                        Pair("headers", record.get("headers").asString())
                    )
                )
            ).also { it.id = InterfaceId(UUID.fromString(record.get("id").asString())) }
        }.all()

    fun findAllIncomingInterfaces() = neo4jClient.query(
        "MATCH (i:$INCOMING_INTERFACE_NODE_LABEL {protocol: \"AMQP\"}) " +
                "RETURN i.id as id, i.`protocolData.queueName` as queueName, i.`protocolData.queueBindings` as queueBindings"
    )
        .fetchAs(IncomingInterface::class.java)
        .mappedBy { _, record ->
            IncomingInterface(
                Protocol("AMQP"),
                ProtocolData(
                    mapOf(
                        Pair("queueName", record.get("queueName").asString()),
                        Pair("queueBindings", record.get("queueBindings").asString())
                    )
                )
            ).also { it.id = InterfaceId(UUID.fromString(record.get("id").asString())) }
        }.all()
}