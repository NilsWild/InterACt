package de.interact.utbi

import de.interact.controller.persistence.domain.INCOMING_INTERFACE_EXPECTATION_NODE_LABEL
import de.interact.controller.persistence.domain.OUTGOING_INTERFACE_EXPECTATION_NODE_LABEL
import de.interact.domain.amqp.*
import de.interact.domain.serialization.SerializationConstants
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Repository
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Repository
class AmqpInterfaceExpectationRepository(private val neo4jClient: Neo4jClient) {
    fun findAllOutgoingAmqpInterfaceExpectations() = neo4jClient.query(
        "MATCH (o:$OUTGOING_INTERFACE_EXPECTATION_NODE_LABEL {protocol: \"AMQP\"}) " +
                "RETURN o.id as id, o.`protocolData.queueName` as queueName, o.`protocolData.queueBindings` as queueBindings"
    )
        .fetchAs(OutgoingAmqpInterfaceExpectation::class.java)
        .mappedBy { _, record ->
            OutgoingAmqpInterfaceExpectation(
                UUID.fromString(record.get("id").asString()),
                IncomingAmqpProtocolData(
                    SerializationConstants.mapper.readValue(
                        record.get("queueBindings").asString().replace("\'", "\""),
                        SerializationConstants.mapper.typeFactory.constructCollectionType(
                            List::class.java,
                            QueueBinding::class.java
                        )
                    )
                )
            )
        }.all()

    fun findAllIncomingAmqpInterfaceExpectations() = neo4jClient.query(
        "MATCH (i:$INCOMING_INTERFACE_EXPECTATION_NODE_LABEL {protocol: \"AMQP\"}) " +
                "RETURN i.id as id, i.`protocolData.exchangeType` as exchangeType, i.`protocolData.exchangeName` as exchangeName, i.`protocolData.routingKey` as routingKey, i.`protocolData.headers` as headers"
    )
        .fetchAs(IncomingAmqpInterfaceExpectation::class.java)
        .mappedBy { _, record ->
            IncomingAmqpInterfaceExpectation(
                UUID.fromString(record.get("id").asString()),
                OutgoingAmqpProtocolData(
                    ExchangeName(record.get("exchangeName").asString()),
                    ExchangeType.valueOf(record.get("exchangeType").asString()),
                    RoutingKey(record.get("routingKey").asString()),
                    SerializationConstants.mapper.readValue(
                        record.get("headers").asString().replace("'", "\""),
                        SerializationConstants.mapper.typeFactory.constructMapType(
                            Map::class.java,
                            String::class.java,
                            String::class.java
                        )
                    )
                )
            )
        }.all()

    fun findIncomingAmqpInterfaceExpectation(id: UUID): IncomingAmqpInterfaceExpectation? {
        return neo4jClient.query(
            "MATCH (i:$INCOMING_INTERFACE_EXPECTATION_NODE_LABEL {protocol: \$id}) " +
                    "RETURN i.id as id, i.`protocolData.exchangeType` as exchangeType, i.`protocolData.exchangeName` as exchangeName, i.`protocolData.routingKey` as routingKey, i.`protocolData.headers` as headers"
        ).bind(id.toString()).to("id")
            .fetchAs(IncomingAmqpInterfaceExpectation::class.java)
            .mappedBy { _, record ->
                IncomingAmqpInterfaceExpectation(
                    UUID.fromString(record.get("id").asString()),
                    OutgoingAmqpProtocolData(
                        ExchangeName(record.get("exchangeName").asString()),
                        ExchangeType.valueOf(record.get("exchangeType").asString()),
                        RoutingKey(record.get("routingKey").asString()),
                        SerializationConstants.mapper.readValue(
                            record.get("headers").asString().replace("'", "\""),
                            SerializationConstants.mapper.typeFactory.constructMapType(
                                Map::class.java,
                                String::class.java,
                                String::class.java
                            )
                        )
                    )
                )
            }.one().getOrNull()
    }

    fun findOutgoingAmqpInterfaceExpectation(id: UUID): OutgoingAmqpInterfaceExpectation? {
        return neo4jClient.query(
            "MATCH (o:$OUTGOING_INTERFACE_EXPECTATION_NODE_LABEL {id: \$id}) " +
                    "RETURN o.id as id, o.`protocolData.queueName` as queueName, o.`protocolData.queueBindings` as queueBindings"
        ).bind(id.toString()).to("id")
            .fetchAs(OutgoingAmqpInterfaceExpectation::class.java)
            .mappedBy { _, record ->
                OutgoingAmqpInterfaceExpectation(
                    UUID.fromString(record.get("id").asString()),
                    IncomingAmqpProtocolData(
                        SerializationConstants.mapper.readValue(
                            record.get("queueBindings").asString().replace("\'", "\""),
                            SerializationConstants.mapper.typeFactory.constructCollectionType(
                                List::class.java,
                                QueueBinding::class.java
                            )
                        )
                    )
                )
            }.one().getOrNull()
    }
}