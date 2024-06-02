package de.interact

import de.interact.controller.persistence.domain.INCOMING_INTERFACE_NODE_LABEL
import de.interact.controller.persistence.domain.IncomingInterfaceEntity
import de.interact.controller.persistence.domain.OUTGOING_INTERFACE_NODE_LABEL
import de.interact.controller.persistence.domain.OutgoingInterfaceEntity
import de.interact.domain.testtwin.api.event.IncomingInterfaceAddedToVersionEvent
import de.interact.domain.testtwin.api.event.InterfaceAddedToVersionEvent
import de.interact.domain.testtwin.api.event.OutgoingInterfaceAddedToVersionEvent
import de.interact.repository.RestInterfaceDao
import de.interact.utbi.InterfaceBinder
import de.interact.utbi.InterfaceBinderName
import de.interact.utbi.InterfaceBinderVersion
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Component

@Component
class RestBinder(private val neo4jClient: Neo4jClient, private val repository: RestInterfaceDao) :
    InterfaceBinder {
    override val name: InterfaceBinderName
        get() = InterfaceBinderName("RESTBinder")
    override val version: InterfaceBinderVersion
        get() = InterfaceBinderVersion("1.0.0")

    override fun bindInterfaces(interfaceAddedEvent: InterfaceAddedToVersionEvent) {
        when (interfaceAddedEvent) {
            is IncomingInterfaceAddedToVersionEvent -> bindIncomingInterface(interfaceAddedEvent)
            is OutgoingInterfaceAddedToVersionEvent -> bindOutgoingInterface(interfaceAddedEvent)
        }
    }

    private fun bindOutgoingInterface(interfaceAddedEvent: OutgoingInterfaceAddedToVersionEvent) {
        val interfaceData = repository.findOutgoingById(interfaceAddedEvent.interfaceId)!!.protocolData
        var query = ""
        interfaceData.forEach { (k, _) ->
            query += ",`${OutgoingInterfaceEntity::protocolData.name}.$k`:\$protocolData$k"
        }
        neo4jClient.query(
            "MATCH (o:$OUTGOING_INTERFACE_NODE_LABEL{${OutgoingInterfaceEntity::id.name}:\$interfaceId}) " +
                    "WITH o " +
                    "MATCH (i:$INCOMING_INTERFACE_NODE_LABEL{${IncomingInterfaceEntity::protocol.name}:\$protocol$query}) " +
                    "MERGE (o)-[:BOUND_TO{createdBy:'$name:$version'}]->(i)"
        )
            .bind(interfaceAddedEvent.interfaceId.toString()).to("interfaceId")
            .bind("REST").to("protocol")
            .bind(interfaceData).with {
                interfaceData.map { entry ->
                    "protocolData${entry.key}" to entry.value
                }.toMap()
            }
            .run()
    }

    private fun bindIncomingInterface(interfaceAddedEvent: IncomingInterfaceAddedToVersionEvent) {
        val interfaceData = repository.findIncomingById(interfaceAddedEvent.interfaceId)!!.protocolData
        var query = ""
        interfaceData.forEach { (k, _) ->
            query += ",`${OutgoingInterfaceEntity::protocolData.name}.$k`:\$protocolData$k"
        }
        neo4jClient.query(
            "MATCH (i:$INCOMING_INTERFACE_NODE_LABEL{${IncomingInterfaceEntity::id.name}:\$interfaceId}) " +
                    "WITH i " +
                    "MATCH (o:$OUTGOING_INTERFACE_NODE_LABEL{${OutgoingInterfaceEntity::protocol.name}:\$protocol$query}) " +
                    "MERGE (o)-[:BOUND_TO{createdBy:'$name:$version'}]->(i)"
        )
            .bind(interfaceAddedEvent.interfaceId.toString()).to("interfaceId")
            .bind("REST").to("protocol")
            .bind(interfaceData).with {
                interfaceData.map { entry ->
                    "protocolData${entry.key}" to entry.value
                }.toMap()
            }
            .run()
    }

    override fun canHandle(interfaceAddedEvent: InterfaceAddedToVersionEvent): Boolean {
        return interfaceAddedEvent.protocol == "REST"
    }
}