package de.rwth.swc.interact.controller.persistence.repository

import de.rwth.swc.interact.controller.persistence.domain.INCOMING_INTERFACE_EXPECTATION_NODE_LABEL
import de.rwth.swc.interact.controller.persistence.domain.IncomingInterfaceExpectationEntity
import de.rwth.swc.interact.controller.persistence.domain.SYSTEM_PROPERTY_EXPECTATION_NODE_LABEL
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
internal interface IncomingInterfaceExpectationRepository : org.springframework.data.repository.Repository<IncomingInterfaceExpectationEntity, UUID>{

    @Query(
        value = "MATCH (spe:$SYSTEM_PROPERTY_EXPECTATION_NODE_LABEL)-[:EXPECT_TO_MATCHING]->(iie:$INCOMING_INTERFACE_EXPECTATION_NODE_LABEL) " +
                "WHERE spe.id=\$id " +
                "RETURN iie.id"
    )
    fun findBySystemPropertyExpectationId(@Param("id") systemPropertyExpectationId: UUID): IncomingInterfaceExpectationEntity?

}