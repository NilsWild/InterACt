package de.rwth.swc.interact.utbi

import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Component

@Component
class RestBinder(private val neo4jClient: Neo4jClient) : InterfaceBinder {
    override val name: InterfaceBinderName
        get() = InterfaceBinderName("RESTBinder")
    override val version: InterfaceBinderVersion
        get() = InterfaceBinderVersion("1.0.0")


    override fun bindInterfaces() {
        neo4jClient.query("MATCH (o:OutgoingInterface), (i:IncomingInterface) WHERE i.protocol = \"REST\" AND o.protocol = \"REST\" AND i.`protocolData.url` = o.`protocolData.url` AND i.`protocolData.request` = o.`protocolData.request` MERGE (o)-[:BOUND_TO{createdBy:'$name:$version'}]->(i)")
            .run()
    }
}