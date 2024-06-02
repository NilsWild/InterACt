package de.interact.repository

import de.interact.controller.persistence.domain.*
import de.interact.domain.shared.IncomingInterfaceId
import de.interact.domain.shared.OutgoingInterfaceId
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import java.util.*

@Repository
interface RestInterfaceRepository : org.springframework.data.repository.Repository<IncomingInterfaceEntity, UUID> {

    @Query("MATCH (i:$INTERFACE_NODE_LABEL{id: \$interfaceId}) MATCH (e:$INTERFACE_EXPECTATION_NODE_LABEL{id: \$expectationId}) MERGE (e)-[:$MATCHED_BY_RELATIONSHIP_LABEL]->(i)")
    fun match(interfaceId: UUID, expectationId: UUID)
}

@Repository
interface IncomingRestInterfaceRepository :
    org.springframework.data.repository.Repository<IncomingInterfaceEntity, UUID> {
    fun findIncomingRestInterfaceByIdAndProtocol(id: UUID, protocol: String): IncomingRestInterface?
    fun findByProtocol(protocol: String): Collection<IncomingRestInterface>
}

@Repository
interface OutgoingRestInterfaceRepository :
    org.springframework.data.repository.Repository<OutgoingInterfaceEntity, UUID> {
    fun findOutgoingRestInterfaceByIdAndProtocol(id: UUID, protocol: String): OutgoingRestInterface?
    fun findByProtocol(protocol: String): Collection<OutgoingRestInterface>
}

@Service
class RestInterfaceDao(
    private val restInterfaceRepository: RestInterfaceRepository,
    private val incomingRestInterfaceRepository: IncomingRestInterfaceRepository,
    private val outgoingRestInterfaceRepository: OutgoingRestInterfaceRepository
) {
    fun findIncomingById(id: IncomingInterfaceId): IncomingRestInterface? {
        return incomingRestInterfaceRepository.findIncomingRestInterfaceByIdAndProtocol(id.value, "REST")
    }

    fun findOutgoingById(id: OutgoingInterfaceId): OutgoingRestInterface? {
        return outgoingRestInterfaceRepository.findOutgoingRestInterfaceByIdAndProtocol(id.value, "REST")
    }

    fun findAllIncoming(): Collection<IncomingRestInterface> {
        return incomingRestInterfaceRepository.findByProtocol("REST")
    }

    fun findAllOutgoing(): Collection<OutgoingRestInterface> {
        return outgoingRestInterfaceRepository.findByProtocol("REST")
    }

    fun match(restInterface: RestInterface, expectation: RestInterfaceExpectation) {
        restInterfaceRepository.match(restInterface.id, expectation.id)
    }
}