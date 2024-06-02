package de.interact.utbi

import de.interact.controller.persistence.domain.INCOMING_INTERFACE_NODE_LABEL
import de.interact.controller.persistence.domain.OUTGOING_INTERFACE_NODE_LABEL
import de.interact.domain.amqp.*
import de.interact.domain.serialization.SerializationConstants
import de.interact.domain.shared.IncomingInterfaceId
import de.interact.domain.shared.OutgoingInterfaceId
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Repository
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Repository
class AmqpInterfaceRepository(private val neo4jClient: Neo4jClient) {
    fun findAllOutgoingAmqpInterfaces() = neo4jClient.query(
        "MATCH (o:$OUTGOING_INTERFACE_NODE_LABEL {protocol: \"AMQP\"}) " +
                "RETURN o.id as id, o.`protocolData.exchangeType` as exchangeType, o.`protocolData.exchangeName` as exchangeName, o.`protocolData.routingKey` as routingKey, o.`protocolData.headers` as headers"
    )
        .fetchAs(OutgoingAmqpInterface::class.java)
        .mappedBy { _, record ->
            OutgoingAmqpInterface(
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

    fun findAllIncomingAmqpInterfaces() = neo4jClient.query(
        "MATCH (i:$INCOMING_INTERFACE_NODE_LABEL {protocol: \"AMQP\"}) " +
                "RETURN i.id as id, i.`protocolData.queueName` as queueName, i.`protocolData.queueBindings` as queueBindings"
    )
        .fetchAs(IncomingAmqpInterface::class.java)
        .mappedBy { _, record ->
            IncomingAmqpInterface(
                UUID.fromString(record.get("id").asString()),
                IncomingAmqpProtocolData(
                    QueueName(record.get("queueName").asString()),
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

    fun findIncomingInterface(
        interfaceId: IncomingInterfaceId
    ): IncomingAmqpInterface? {

        return neo4jClient.query(
            "MATCH (i:$INCOMING_INTERFACE_NODE_LABEL{id:\$interfaceId}) " +
                    "RETURN i"
        ).bind(
            interfaceId.toString()
        ).to("interfaceId")
            .fetchAs(IncomingAmqpInterface::class.java).mappedBy { _, record ->
                val incomingInterface = record.get("i").asNode()
                IncomingAmqpInterface(
                    UUID.fromString(incomingInterface.get("id").asString()),
                    IncomingAmqpProtocolData(
                        QueueName(incomingInterface.get("protocolData.queueName").asString()),
                        SerializationConstants.mapper.readValue(
                            incomingInterface.get("protocolData.queueBindings").asString().replace("\'", "\""),
                            SerializationConstants.mapper.typeFactory.constructCollectionType(
                                List::class.java,
                                QueueBinding::class.java
                            )
                        )
                    )
                )
            }.one().getOrNull()
    }

    fun findOutgoingInterface(
        interfaceId: OutgoingInterfaceId
    ): OutgoingAmqpInterface? {
        return neo4jClient.query(
            "MATCH (o:$OUTGOING_INTERFACE_NODE_LABEL{id:\$interfaceId}) " +
                    "RETURN o"
        ).bind(
            interfaceId.toString()
        ).to("interfaceId")
            .fetchAs(OutgoingAmqpInterface::class.java).mappedBy { _, record ->
                val outgoingInterface = record.get("o").asNode()
                OutgoingAmqpInterface(
                    UUID.fromString(outgoingInterface.get("id").asString()),
                    OutgoingAmqpProtocolData(
                        ExchangeName(outgoingInterface.get("protocolData.exchangeName").asString()),
                        ExchangeType.valueOf(outgoingInterface.get("protocolData.exchangeType").asString()),
                        RoutingKey(outgoingInterface.get("protocolData.routingKey").asString()),
                        SerializationConstants.mapper.readValue(
                            outgoingInterface.get("protocolData.headers").asString().replace("'", "\""),
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
}