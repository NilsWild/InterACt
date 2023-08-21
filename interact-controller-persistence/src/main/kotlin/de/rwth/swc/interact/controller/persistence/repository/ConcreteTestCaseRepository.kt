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
internal interface ConcreteTestCaseRepository :
    org.springframework.data.repository.Repository<ConcreteTestCaseEntity, UUID> {

    @Query(
        value = "MATCH (atc:$ABSTRACT_TEST_CASE_NODE_LABEL)-[:USED_TO_DERIVE]->(ctc:$CONCRETE_TEST_CASE_NODE_LABEL) " +
                "WHERE atc.id=\$id AND ctc.parameters = \$parameters " +
                "RETURN ctc"
    )
    fun findByAbstractTestCaseIdAndParameters(
        @Param("id") abstractTestCaseId: UUID,
        @Param("parameters") parameters: String,
    ): ConcreteTestCaseEntity?

    fun save(concreteTestCase: ConcreteTestCaseEntity): ConcreteTestCaseEntity

    @Query(
        value = "UNWIND range(0, size(\$messageIds)-1) as idx " +
                "WITH idx, \$messageIds[idx] as mId " +
                "MATCH (ctc:$CONCRETE_TEST_CASE_NODE_LABEL{id:\$concreteTestCaseId}), (m:$MESSAGE_NODE_LABEL{id:mId}) " +
                "MERGE (ctc)-[:TRIGGERED{order:idx}]->(m)"
    )
    fun addMessages(
        @Param("concreteTestCaseId") concreteTestCaseId: UUID,
        @Param("messageIds") messageIds: List<UUID>
    )

    fun findById(id: UUID): ConcreteTestCaseEntity?

    @Query(
        "MATCH (ctc:$CONCRETE_TEST_CASE_NODE_LABEL)-[:TRIGGERED]->(m:$MESSAGE_NODE_LABEL) " +
                "WHERE m.id=\$id " +
                "RETURN ctc"
    )
    fun findByTriggeredMessage(@Param("id") id: UUID): ConcreteTestCaseEntity?
}