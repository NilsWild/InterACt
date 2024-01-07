package de.rwth.swc.interact.utbi

import de.rwth.swc.interact.controller.persistence.domain.INCOMING_INTERFACE_NODE_LABEL
import de.rwth.swc.interact.controller.persistence.domain.OUTGOING_INTERFACE_NODE_LABEL
import de.rwth.swc.interact.domain.*
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Component
import java.util.*

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

    override fun bindInterfaces(componentInterface: ComponentInterface) {
        when (componentInterface) {
            is IncomingInterface -> bindInterfaces(
                amqpInterfaceRepository.findAllOutgoingInterfaces(), listOf(
                    componentInterface
                )
            )

            is OutgoingInterface -> bindInterfaces(
                listOf(componentInterface),
                amqpInterfaceRepository.findAllIncomingInterfaces()
            )
        }
    }

    private fun bindInterfaces(
        outgoingInterfaces: Collection<OutgoingInterface>,
        incomingInterfaces: Collection<IncomingInterface>
    ) {
        for (outgoingInterface in outgoingInterfaces) {
            for (incomingInterface in incomingInterfaces) {
                if (canHandle(outgoingInterface) && canHandle(incomingInterface)) {
                    if (amqpInterfaceMatcher.matchAMQPInterfaces(
                            outgoingInterface.protocolData,
                            incomingInterface.protocolData
                        )
                    ) {
                        neo4jClient.query(
                            "MATCH (o:$OUTGOING_INTERFACE_NODE_LABEL {id: \$outId}) " +
                                    "OPTIONAL MATCH (i:$INCOMING_INTERFACE_NODE_LABEL {id: \$inId}) " +
                                    "MERGE (o)-[:BOUND_TO{createdBy:'$name:$version'}]->(i)"
                        ).bind(outgoingInterface.id!!.id.toString()).to("outId")
                            .bind(incomingInterface.id!!.id.toString()).to("inId")
                            .run()
                    }
                }
            }
        }
    }

    override fun canHandle(componentInterface: ComponentInterface): Boolean {
        return componentInterface.protocol.protocol == "AMQP"
    }
}
