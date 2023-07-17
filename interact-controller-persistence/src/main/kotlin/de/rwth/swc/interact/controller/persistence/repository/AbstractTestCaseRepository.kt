package de.rwth.swc.interact.controller.persistence.repository

import de.rwth.swc.interact.controller.persistence.domain.ABSTRACT_TEST_CASE_NODE_LABEL
import de.rwth.swc.interact.controller.persistence.domain.AbstractTestCaseEntity
import de.rwth.swc.interact.controller.persistence.domain.COMPONENT_NODE_LABEL
import de.rwth.swc.interact.controller.persistence.domain.CONCRETE_TEST_CASE_NODE_LABEL
import de.rwth.swc.interact.domain.*
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
internal interface AbstractTestCaseRepository : org.springframework.data.repository.Repository<AbstractTestCaseEntity, UUID> {

    @Query(
        value = "MATCH (c:$COMPONENT_NODE_LABEL)-[:TESTED_BY]->(atc:$ABSTRACT_TEST_CASE_NODE_LABEL) " +
                "WHERE c.id=\$id AND atc.source=\$source AND atc.name=\$name " +
                "RETURN atc.id"
    )
    fun findIdByComponentIdSourceAndName(
        @Param("id") componentId: UUID,
        @Param("source") source: String,
        @Param("name") name: String
    ): UUID?

    fun save(abstractTestCase: AbstractTestCaseEntity): AbstractTestCaseEntity

    @Query(
        value = "MATCH (atc:$ABSTRACT_TEST_CASE_NODE_LABEL) " +
                "WHERE atc.id=\$abstractTestCaseId " +
                "WITH atc " +
                "MATCH (ctc:$CONCRETE_TEST_CASE_NODE_LABEL) " +
                "WHERE ctc.id=\$concreteTestCaseId " +
                "MERGE (atc)-[:USED_TO_DERIVE]->(ctc)"
    )
    fun addConcreteTestCase(
        @Param("abstractTestCaseId") abstractTestCaseId: UUID,
        @Param("concreteTestCaseId") id: UUID
    )
}