package de.rwth.swc.interact.controller.persistence.repository

import de.rwth.swc.interact.controller.persistence.domain.INTERACTION_EXPECTATION_NODE_LABEL
import de.rwth.swc.interact.controller.persistence.domain.InteractionExpectationEntity
import de.rwth.swc.interact.domain.InteractionExpectationId
import de.rwth.swc.interact.domain.ExpectationValidationPlanId
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
internal interface InteractionExpectationRepository :
    org.springframework.data.repository.Repository<InteractionExpectationEntity, UUID> {

    @Query(
        "MATCH (ie:$INTERACTION_EXPECTATION_NODE_LABEL{id:\$interactionExpectationId}) " +
                "MATCH (vp:InteractionExpectationValidationPlan{id:\$validationPlanId}) " +
                "MERGE (ie)-[:POTENTIALLY_VALIDATED_BY]->(vp)"
    )
    fun addValidationPlan(
        interactionExpectationId: InteractionExpectationId,
        validationPlanId: ExpectationValidationPlanId
    )

}