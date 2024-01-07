package de.rwth.swc.interact.controller.persistence.repository

import de.rwth.swc.interact.controller.persistence.domain.INTERACTION_EXPECTATION_VALIDATION_PLAN_NODE_LABEL
import de.rwth.swc.interact.controller.persistence.domain.ExpectationValidationPlanEntity
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
internal interface ExpectationValidationPlanRepository :
    org.springframework.data.repository.Repository<ExpectationValidationPlanEntity, UUID> {
    fun existsByInteractionPathInfo(pathInfo: String): Boolean
    fun save(toEntity: ExpectationValidationPlanEntity): ExpectationValidationPlanEntity
    fun findByNextTest(writeValueAsString: String): Collection<ExpectationValidationPlanEntity>
    fun findById(id: UUID): ExpectationValidationPlanEntity

    @Query(
        "MATCH (vp:$INTERACTION_EXPECTATION_VALIDATION_PLAN_NODE_LABEL{id:\$id}) SET vp.nextComponent=\$componentId"
    )
    fun setNextComponent(@Param("id") id: UUID, @Param("componentId") componentId: UUID)
}