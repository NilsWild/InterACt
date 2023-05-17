package de.rwth.swc.interact.controller.persistence.repository

import de.rwth.swc.interact.controller.persistence.domain.ConcreteTestCase
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ConcreteTestCaseRepository : org.springframework.data.repository.Repository<ConcreteTestCase, UUID> {

    @Query(
        value = "MATCH (atc:AbstractTestCase)-[:USED_TO_DERIVE]->(ctc:ConcreteTestCase) " +
                "WHERE atc.id=\$id AND ctc.name=\$name AND ctc.source=\$source " +
                "RETURN ctc.id"
    )
    fun findIdByAbstractTestCaseIdAndNameAndSource(
        @Param("id") abstractTestCaseId: UUID,
        @Param("name") name: String,
        @Param("source") source: ConcreteTestCase.DataSource
    ): UUID?

    fun save(concreteTestCase: ConcreteTestCase): ConcreteTestCase

    @Query(
        value = "UNWIND \$messageIds as mId " +
                "MATCH (ctc:ConcreteTestCase), (m:Message) " +
                "WHERE ctc.id=\$concreteTestCaseId AND m.id=mId " +
                "MERGE (ctc)-[:TRIGGERED]->(m)"
    )
    fun addMessages(
        @Param("concreteTestCaseId") concreteTestCaseId: UUID,
        @Param("messageIds") messageIds: Collection<UUID>
    )
}