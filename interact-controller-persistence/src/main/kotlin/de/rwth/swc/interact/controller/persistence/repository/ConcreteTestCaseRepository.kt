package de.rwth.swc.interact.controller.persistence.repository

import de.rwth.swc.interact.controller.persistence.domain.ABSTRACT_TEST_CASE_NODE_LABEL
import de.rwth.swc.interact.controller.persistence.domain.CONCRETE_TEST_CASE_NODE_LABEL
import de.rwth.swc.interact.controller.persistence.domain.ConcreteTestCaseEntity
import de.rwth.swc.interact.controller.persistence.domain.MESSAGE_NODE_LABEL
import de.rwth.swc.interact.domain.*
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
internal interface ConcreteTestCaseRepository : org.springframework.data.repository.Repository<ConcreteTestCaseEntity, UUID> {

    @Query(
        value = "MATCH (atc:$ABSTRACT_TEST_CASE_NODE_LABEL)-[:USED_TO_DERIVE]->(ctc:$CONCRETE_TEST_CASE_NODE_LABEL) " +
                "WHERE atc.id=\$id AND ctc.name=\$name AND ctc.mode=\$mode " +
                "RETURN ctc.id"
    )
    fun findIdByAbstractTestCaseIdAndNameAndMode(
        @Param("id") abstractTestCaseId: UUID,
        @Param("name") name: String,
        @Param("mode") mode: TestMode
    ): UUID?

    fun save(concreteTestCase: ConcreteTestCaseEntity): ConcreteTestCaseEntity

    @Query(
        value = "UNWIND \$messageIds as mId " +
                "MATCH (ctc:$CONCRETE_TEST_CASE_NODE_LABEL), (m:$MESSAGE_NODE_LABEL) " +
                "WHERE ctc.id=\$concreteTestCaseId AND m.id=mId " +
                "MERGE (ctc)-[:TRIGGERED]->(m)"
    )
    fun addMessages(
        @Param("concreteTestCaseId") concreteTestCaseId: UUID,
        @Param("messageIds") messageIds: Collection<UUID>
    )
}