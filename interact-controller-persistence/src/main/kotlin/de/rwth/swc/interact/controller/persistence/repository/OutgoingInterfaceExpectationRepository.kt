package de.rwth.swc.interact.controller.persistence.repository

import de.rwth.swc.interact.controller.persistence.domain.*
import de.rwth.swc.interact.controller.persistence.domain.OutgoingInterfaceExpectationEntity
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
internal interface OutgoingInterfaceExpectationRepository : org.springframework.data.repository.Repository<OutgoingInterfaceExpectationEntity, UUID>{

    @Query(
        value = "MATCH (spe:$SYSTEM_PROPERTY_EXPECTATION_NODE_LABEL)-[:EXPECT_FROM_MATCHING]->(oie:$OUTGOING_INTERFACE_EXPECTATION_NODE_LABEL) " +
                "WHERE spe.id=\$id " +
                "RETURN oie.id"
    )
    fun findBySystemPropertyExpectationId(@Param("id") systemPropertyExpectationId: UUID): OutgoingInterfaceExpectationEntity?

}