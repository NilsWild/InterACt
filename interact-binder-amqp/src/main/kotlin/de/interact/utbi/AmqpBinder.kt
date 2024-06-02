package de.interact.utbi

import de.interact.controller.persistence.domain.INCOMING_INTERFACE_NODE_LABEL
import de.interact.controller.persistence.domain.OUTGOING_INTERFACE_NODE_LABEL
import de.interact.domain.amqp.IncomingAmqpInterface
import de.interact.domain.amqp.OutgoingAmqpInterface
import de.interact.domain.testtwin.api.event.IncomingInterfaceAddedToVersionEvent
import de.interact.domain.testtwin.api.event.InterfaceAddedToVersionEvent
import de.interact.domain.testtwin.api.event.OutgoingInterfaceAddedToVersionEvent
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Component

@Component
class AmqpBinder(
    private val neo4jClient: Neo4jClient,
    private val amqpInterfaceMatcher: AmqpInterfaceMatcher,
    private val amqpInterfaceRepository: AmqpInterfaceRepository
) : InterfaceBinder {
    override val name: InterfaceBinderName
        get() = InterfaceBinderName("AmqpBinder")
    override val version: InterfaceBinderVersion
        get() = InterfaceBinderVersion("1.0.0")

    override fun bindInterfaces(interfaceAddedEvent: InterfaceAddedToVersionEvent) {
        when (interfaceAddedEvent) {
            is IncomingInterfaceAddedToVersionEvent -> bindInterfaces(
                amqpInterfaceRepository.findAllOutgoingAmqpInterfaces(),
                listOf(
                    amqpInterfaceRepository.findIncomingInterface(
                        interfaceAddedEvent.interfaceId
                    )!!
                )
            )

            is OutgoingInterfaceAddedToVersionEvent -> bindInterfaces(
                listOf(
                    amqpInterfaceRepository.findOutgoingInterface(
                        interfaceAddedEvent.interfaceId
                    )!!
                ),
                amqpInterfaceRepository.findAllIncomingAmqpInterfaces()
            )
        }
    }

    private fun bindInterfaces(
        outgoingInterfaces: Collection<OutgoingAmqpInterface>,
        incomingInterfaces: Collection<IncomingAmqpInterface>
    ) {
        for (outgoingInterface in outgoingInterfaces) {
            for (incomingInterface in incomingInterfaces) {

                if (amqpInterfaceMatcher.matchAMQPInterfaces(
                        outgoingInterface.protocolData,
                        incomingInterface.protocolData
                    )
                ) {
                    neo4jClient.query(
                        "MATCH (o:$OUTGOING_INTERFACE_NODE_LABEL {id: \$outId}) " +
                                "OPTIONAL MATCH (i:$INCOMING_INTERFACE_NODE_LABEL {id: \$inId}) " +
                                "MERGE (o)-[:BOUND_TO{createdBy:'$name:$version'}]->(i)"
                    ).bind(outgoingInterface.id.toString()).to("outId")
                        .bind(incomingInterface.id.toString()).to("inId")
                        .run()
                }

            }
        }
    }

    override fun canHandle(interfaceAddedEvent: InterfaceAddedToVersionEvent): Boolean {
        return interfaceAddedEvent.protocol == "AMQP"
    }
}
