package de.rwth.swc.interact.utbi

import de.rwth.swc.interact.controller.persistence.domain.INCOMING_INTERFACE_EXPECTATION_NODE_LABEL
import de.rwth.swc.interact.controller.persistence.domain.OUTGOING_INTERFACE_EXPECTATION_NODE_LABEL
import de.rwth.swc.interact.domain.*
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
class AmqpInterfaceExpectationRepository(private val neo4jClient: Neo4jClient) {
    fun findAllOutgoingInterfaceExpectations() = neo4jClient.query(
        "MATCH (o:$OUTGOING_INTERFACE_EXPECTATION_NODE_LABEL {protocol: \"AMQP\"}) " +
                "RETURN o.id as id, o.`protocolData.exchangeType` as exchangeType, o.`protocolData.exchangeName` as exchangeName, o.`protocolData.routingKey` as routingKey, o.`protocolData.headers` as headers"
    )
        .fetchAs(OutgoingInterfaceExpectation::class.java)
        .mappedBy { _, record ->
            OutgoingInterfaceExpectation(
                Protocol("AMQP"),
                ProtocolData(
                    mapOf(
                        Pair("exchangeType", record.get("exchangeType").asString()),
                        Pair("exchangeName", record.get("exchangeName").asString()),
                        Pair("routingKey", record.get("routingKey").asString()),
                        Pair("headers", record.get("headers").asString())
                    )
                )
            ).also { it.id = InterfaceExpectationId(UUID.fromString(record.get("id").asString())) }
        }.all()

    fun findAllIncomingInterfaceExpectations() = neo4jClient.query(
        "MATCH (i:$INCOMING_INTERFACE_EXPECTATION_NODE_LABEL {protocol: \"AMQP\"}) " +
                "RETURN i.id as id, i.`protocolData.queueName` as queueName, i.`protocolData.queueBindings` as queueBindings"
    )
        .fetchAs(IncomingInterfaceExpectation::class.java)
        .mappedBy { _, record ->
            IncomingInterfaceExpectation(
                Protocol("AMQP"),
                ProtocolData(
                    mapOf(
                        Pair("queueName", record.get("queueName").asString()),
                        Pair("queueBindings", record.get("queueBindings").asString())
                    )
                )
            ).also { it.id = InterfaceExpectationId(UUID.fromString(record.get("id").asString())) }
        }.all()
}