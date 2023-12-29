package de.rwth.swc.interact.utbi

import de.rwth.swc.interact.domain.*
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Component
import java.util.*

@Component
class AMQPBinder(
    private val neo4jClient: Neo4jClient,
    private val amqpInterfaceMatcher: AMQPInterfaceMatcher
) : InterfaceBinder {
    override val name: InterfaceBinderName
        get() = InterfaceBinderName("AMQPBinder")
    override val version: InterfaceBinderVersion
        get() = InterfaceBinderVersion("1.0.0")

    override fun bindInterfaces(componentInterface: ComponentInterface) {
        when (componentInterface) {
            is IncomingInterface -> bindInterfaces(
                findAllOutgoingInterfaces(), listOf(
                    componentInterface
                )
            )

            is OutgoingInterface -> bindInterfaces(
                listOf(componentInterface),
                findAllIncomingInterfaces()
            )
        }
    }

    private fun bindInterfaces(
        outgoingInterfaces: Collection<OutgoingInterface>,
        incomingInterfaces: Collection<IncomingInterface>
    ) {
        for (outgoingInterface in outgoingInterfaces) {
            for (incomingInterface in incomingInterfaces) {
                if (amqpInterfaceMatcher.matchAMQPInterfaces(outgoingInterface, incomingInterface)) {
                    neo4jClient.query(
                        "MATCH (o:OutgoingInterface {id: \$outId}) " +
                                "OPTIONAL MATCH (i:IncomingInterface {id: \$inId}) " +
                                "MERGE (o)-[:BOUND_TO{createdBy:'$name:$version'}]->(i)"
                    ).bind(outgoingInterface.id!!.id.toString()).to("outId")
                        .bind(incomingInterface.id!!.id.toString()).to("inId")
                        .run()
                }
            }
        }
    }

    private fun findAllOutgoingInterfaces() = neo4jClient.query(
        "MATCH (o:OutgoingInterface {protocol: \"AMQP\"}) " +
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

    private fun findAllIncomingInterfaces() = neo4jClient.query(
        "MATCH (i:IncomingInterface {protocol: \"AMQP\"}) " +
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


    override fun canHandle(componentInterface: ComponentInterface): Boolean {
        return componentInterface.protocol.protocol == "AMQP"
    }
}
