package de.rwth.swc.interact.controller.persistence.repository

import de.rwth.swc.interact.controller.persistence.domain.*
import de.rwth.swc.interact.controller.persistence.domain.SystemPropertyExpectationEntity
import de.rwth.swc.interact.domain.SystemPropertyExpectationId
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
internal interface SystemPropertyExpectationRepository :
org.springframework.data.repository.Repository<SystemPropertyExpectationEntity, UUID> {
    @Query(
        value = "MATCH (c:$COMPONENT_NODE_LABEL)-[:EXPECTS]->(spe:$SYSTEM_PROPERTY_EXPECTATION_NODE_LABEL) " +
                "WHERE c.id=\$id AND spe.source=\$source AND spe.name=\$name " +
                "RETURN spe.id"
    )
    fun findIdByComponentIdSourceAndName(
        @Param("id") componentId: UUID,
        @Param("source") source: String,
        @Param("name") name: String
    ): SystemPropertyExpectationEntity?

    @Query(
        value = "MATCH (spe:${SYSTEM_PROPERTY_EXPECTATION_NODE_LABEL}), (ie:$INTERFACE_EXPECTATION_NODE_LABEL) " +
                "WHERE spe.id=\$speId AND ie.id=\$ieId " +
                "MERGE (spe)-[:EXPECT_FROM_MATCHING]->(ie)"
    )
    fun addFromExpectation(@Param("speId") systemPropertyExpectationId: UUID, @Param("ieId") interfaceExpectationId: UUID)

    @Query(
        value = "MATCH (spe:${SYSTEM_PROPERTY_EXPECTATION_NODE_LABEL}), (ie:$INTERFACE_EXPECTATION_NODE_LABEL) " +
                "WHERE spe.id=\$speId AND ie.id=\$ieId " +
                "MERGE (spe)-[:EXPECT_TO_MATCHING]->(ie)"
    )
    fun addToExpectation(@Param("speId") systemPropertyExpectationId: UUID, @Param("ieId") interfaceExpectationId: UUID)

}