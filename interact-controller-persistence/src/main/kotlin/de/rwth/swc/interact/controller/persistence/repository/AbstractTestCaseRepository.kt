package de.rwth.swc.interact.controller.persistence.repository

import de.rwth.swc.interact.controller.persistence.domain.AbstractTestCase
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AbstractTestCaseRepository : org.springframework.data.repository.Repository<AbstractTestCase, UUID> {

    @Query(
        value = "MATCH (c:Component)-[:TESTED_BY]->(atc:AbstractTestCase) " +
                "WHERE c.id=\$id AND atc.source=\$source AND atc.name=\$name " +
                "RETURN atc.id"
    )
    fun findIdByComponentIdSourceAndName(
        @Param("id") componentId: UUID,
        @Param("source") source: String,
        @Param("name") name: String
    ): UUID?

    fun save(abstractTestCase: AbstractTestCase): AbstractTestCase

    @Query(
        value = "MATCH (atc:AbstractTestCase) " +
                "WHERE atc.id=\$abstractTestCaseId " +
                "WITH atc " +
                "MATCH (ctc:ConcreteTestCase) " +
                "WHERE ctc.id=\$concreteTestCaseId " +
                "MERGE (atc)-[:USED_TO_DERIVE]->(ctc)"
    )
    fun addConcreteTestCase(
        @Param("abstractTestCaseId") abstractTestCaseId: UUID,
        @Param("concreteTestCaseId") id: UUID
    )
}