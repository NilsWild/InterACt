package de.rwth.swc.interact.controller.persistence.repository

import de.rwth.swc.interact.controller.persistence.domain.OutgoingInterfaceEntity
import de.rwth.swc.interact.domain.MessageType
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
internal interface OutgoingInterfaceRepository : org.springframework.data.repository.Repository<OutgoingInterfaceEntity, UUID> {
    fun save(outgoingInterface: OutgoingInterfaceEntity): OutgoingInterfaceEntity

    @Query(
        "MATCH (m{id:\$id})-[:SENT_BY]->(oi) " +
        "RETURN oi"
    )
    fun findByMessage(@Param("id") id: UUID): OutgoingInterfaceEntity?
}