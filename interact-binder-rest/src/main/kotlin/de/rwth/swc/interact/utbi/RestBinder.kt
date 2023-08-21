package de.rwth.swc.interact.utbi

import de.rwth.swc.interact.domain.ComponentInterface
import de.rwth.swc.interact.domain.IncomingInterface
import de.rwth.swc.interact.domain.OutgoingInterface
import de.rwth.swc.interact.domain.Protocol
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Component

@Component
class RestBinder(private val neo4jClient: Neo4jClient) : InterfaceBinder {
    override val name: InterfaceBinderName
        get() = InterfaceBinderName("RESTBinder")
    override val version: InterfaceBinderVersion
        get() = InterfaceBinderVersion("1.0.0")


    override fun bindInterfaces(componentInterface: ComponentInterface) {
        when(componentInterface) {
            is IncomingInterface -> bindIncomingInterface(componentInterface)
            is OutgoingInterface -> bindOutgoingInterface(componentInterface)
        }
    }

    private fun bindOutgoingInterface(outgoingInterface: OutgoingInterface) {
        neo4jClient.query(
            "MATCH (o:OutgoingInterface{id:\$outId}), (i:IncomingInterface) " +
                    "WHERE i.protocol = \"REST\" " +
                    "AND o.protocol = \"REST\" " +
                    "AND i.`protocolData.url` = o.`protocolData.url` " +
                    "AND i.`protocolData.request` = o.`protocolData.request` " +
                    "AND i.`protocolData.method` = o.`protocolData.method` " +
                    "MERGE (o)-[:BOUND_TO{createdBy:'$name:$version'}]->(i)")
            .bind(outgoingInterface.id!!.id.toString()).to("outId")
            .run()
    }

    private fun bindIncomingInterface(incomingInterface: IncomingInterface) {
        neo4jClient.query(
            "MATCH (o:OutgoingInterface), (i:IncomingInterface{id:\$inId}) " +
                    "WHERE i.protocol = \"REST\" " +
                    "AND o.protocol = \"REST\" " +
                    "AND i.`protocolData.url` = o.`protocolData.url` " +
                    "AND i.`protocolData.request` = o.`protocolData.request` " +
                    "AND i.`protocolData.method` = o.`protocolData.method` " +
                    "MERGE (o)-[:BOUND_TO{createdBy:'$name:$version'}]->(i)")
            .bind(incomingInterface.id!!.id.toString()).to("inId")
            .run()
    }

    override fun canHandle(componentInterface: ComponentInterface): Boolean {
        return componentInterface.protocol.protocol == "REST"
    }
}