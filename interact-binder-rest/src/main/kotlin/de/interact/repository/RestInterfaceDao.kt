package de.interact.repository

import de.interact.controller.persistence.domain.*
import de.interact.domain.rest.IncomingRestInterface
import de.interact.domain.rest.OutgoingRestInterface
import de.interact.domain.shared.IncomingInterfaceId
import de.interact.domain.shared.OutgoingInterfaceId
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import java.util.*

@Repository
interface RestInterfaceRepository : org.springframework.data.repository.Repository<IncomingInterfaceEntity, UUID> {

    @Query("MATCH (i:$INTERFACE_NODE_LABEL{id: \$interfaceId}) " +
            "MATCH (e:$INTERFACE_EXPECTATION_NODE_LABEL{id: \$expectationId}) " +
            "MERGE (e)-[:$MATCHED_BY_RELATIONSHIP_LABEL]->(i)")
    fun match(interfaceId: UUID, expectationId: UUID)
    @Query("MATCH (i:$INCOMING_INTERFACE_NODE_LABEL{id:\$incomingInterface}) " +
            "WITH i " +
            "MATCH (o:$OUTGOING_INTERFACE_NODE_LABEL{id:\$outgoingInterface}) " +
            "MERGE (o)-[:BOUND_TO]->(i)")
    fun bind(outgoingInterface: UUID, incomingInterface: UUID)
}

@Repository
interface IncomingRestInterfaceRepository :
    org.springframework.data.repository.Repository<IncomingInterfaceEntity, UUID> {
    fun findIncomingRestInterfaceByIdAndProtocol(id: UUID, protocol: String): IncomingRestInterfaceProjection?
    fun findByProtocol(protocol: String): Collection<IncomingRestInterfaceProjection>
}

@Repository
interface OutgoingRestInterfaceRepository :
    org.springframework.data.repository.Repository<OutgoingInterfaceEntity, UUID> {
    fun findOutgoingRestInterfaceByIdAndProtocol(id: UUID, protocol: String): OutgoingRestInterfaceProjection?
    fun findByProtocol(protocol: String): Collection<OutgoingRestInterfaceProjection>
}

@Service
class RestInterfaceDao(
    private val restInterfaceRepository: RestInterfaceRepository,
    private val incomingRestInterfaceRepository: IncomingRestInterfaceRepository,
    private val outgoingRestInterfaceRepository: OutgoingRestInterfaceRepository
) {
    fun findIncomingById(id: IncomingInterfaceId): IncomingRestInterfaceProjection? {
        return incomingRestInterfaceRepository.findIncomingRestInterfaceByIdAndProtocol(id.value, "REST")
    }

    fun findOutgoingById(id: OutgoingInterfaceId): OutgoingRestInterfaceProjection? {
        return outgoingRestInterfaceRepository.findOutgoingRestInterfaceByIdAndProtocol(id.value, "REST")
    }

    fun findAllIncoming(): Collection<IncomingRestInterfaceProjection> {
        return incomingRestInterfaceRepository.findByProtocol("REST")
    }

    fun findAllOutgoing(): Collection<OutgoingRestInterfaceProjection> {
        return outgoingRestInterfaceRepository.findByProtocol("REST")
    }

    fun match(restInterface: RestInterfaceProjection, expectation: RestInterfaceExpectationProjection) {
        restInterfaceRepository.match(restInterface.id, expectation.id)
    }

    fun bind(outgoingInterface: OutgoingRestInterface, incomingInterface: IncomingRestInterface) {
        restInterfaceRepository.bind(outgoingInterface.id.value, incomingInterface.id.value)
    }
}