package de.rwth.swc.interact.controller.persistence.repository

import de.rwth.swc.interact.controller.persistence.domain.INTERACTION_EXPECTATION_VALIDATION_PLAN_NODE_LABEL
import de.rwth.swc.interact.controller.persistence.domain.InteractionExpectationValidationPlanEntity
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
internal interface InteractionExpectationValidationPlanRepository: org.springframework.data.repository.Repository<InteractionExpectationValidationPlanEntity, UUID> {
    fun existsByInteractionPathInfo(pathInfo: String): Boolean
    fun save(toEntity: InteractionExpectationValidationPlanEntity): InteractionExpectationValidationPlanEntity
    fun findByNextTest(writeValueAsString: String): Collection<InteractionExpectationValidationPlanEntity>
    fun findById(id: UUID): InteractionExpectationValidationPlanEntity
    @Query(
        "MATCH (vp:$INTERACTION_EXPECTATION_VALIDATION_PLAN_NODE_LABEL{id:\$id}) SET vp.nextComponent=\$componentId"
    )
    fun setNextComponent(@Param("id") id: UUID, @Param("componentId") componentId: UUID)
}