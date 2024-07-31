package de.interact

import de.interact.domain.testtwin.api.event.IncomingInterfaceAddedToVersionEvent
import de.interact.domain.testtwin.api.event.InterfaceAddedToVersionEvent
import de.interact.domain.testtwin.api.event.OutgoingInterfaceAddedToVersionEvent
import de.interact.repository.RestInterfaceDao
import de.interact.utbi.InterfaceBinder
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Component

@Component
class RestBinder(
    private val neo4jClient: Neo4jClient,
    private val repository: RestInterfaceDao,
    private val restInterfaceMatcher: RestInterfaceMatcher
): InterfaceBinder {

    override fun bindInterfaces(interfaceAddedEvent: InterfaceAddedToVersionEvent) {
        when (interfaceAddedEvent) {
            is IncomingInterfaceAddedToVersionEvent -> bindIncomingInterface(interfaceAddedEvent)
            is OutgoingInterfaceAddedToVersionEvent -> bindOutgoingInterface(interfaceAddedEvent)
        }
    }

    private fun bindOutgoingInterface(interfaceAddedEvent: OutgoingInterfaceAddedToVersionEvent) {
        val interfaceData = repository.findOutgoingById(interfaceAddedEvent.interfaceId)!!.toDomain()
        val incomingInterfaces = repository.findAllIncoming().map { it.toDomain() }
        val matched = incomingInterfaces.filter { restInterfaceMatcher.matches(interfaceData, it ) }
        matched.forEach{
            repository.bind(interfaceData, it)
        }
    }

    private fun bindIncomingInterface(interfaceAddedEvent: IncomingInterfaceAddedToVersionEvent) {
        val interfaceData = repository.findIncomingById(interfaceAddedEvent.interfaceId)!!.toDomain()
        val outgoingInterfaces = repository.findAllOutgoing().map { it.toDomain() }
        val matched = outgoingInterfaces.filter { restInterfaceMatcher.matches(interfaceData, it ) }
        matched.forEach{
            repository.bind(it, interfaceData)
        }
    }

    override fun canHandle(interfaceAddedEvent: InterfaceAddedToVersionEvent): Boolean {
        return interfaceAddedEvent.protocol == "REST"
    }
}