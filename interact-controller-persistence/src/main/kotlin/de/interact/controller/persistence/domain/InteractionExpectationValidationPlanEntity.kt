package de.interact.controller.persistence.domain

import de.interact.domain.shared.EntityReference
import de.interact.domain.shared.EntityReferenceProjection
import de.interact.domain.shared.ValidationPlanId
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship

const val INTERACTION_EXPECTATION_VALIDATION_PLAN_NODE_LABEL = "InteractionExpectationValidationPlan"
const val CANDIDATE_FOR_RELATIONSHIP_LABEL = "CANDIDATE_FOR"
const val INTERACTION_GRAPH_RELATIONSHIP_LABEL = "INTERACTION_GRAPH"


@Node(INTERACTION_EXPECTATION_VALIDATION_PLAN_NODE_LABEL)
class InteractionExpectationValidationPlanEntity: Entity() {

    @Relationship(CANDIDATE_FOR_RELATIONSHIP_LABEL)
    lateinit var candidateFor: InteractionExpectationEntity

    @Relationship(INTERACTION_GRAPH_RELATIONSHIP_LABEL)
    lateinit var interactionGraph: InteractionGraphEntity

    lateinit var status: String

}

fun interactionExpectationValidationPlanEntityReference(
    id: ValidationPlanId,
    version: Long?
): InteractionExpectationValidationPlanEntity {
    return InteractionExpectationValidationPlanEntity().also {
        it.id = id.value
        it.version = version
    }
}

fun interactionExpectationValidationPlanEntity(
    id: ValidationPlanId,
    version: Long?,
    candidateFor: InteractionExpectationEntity,
    interactionGraph: InteractionGraphEntity,
    status: String
): InteractionExpectationValidationPlanEntity {
    return interactionExpectationValidationPlanEntityReference(id,version).also {
        it.candidateFor = candidateFor
        it.interactionGraph = interactionGraph
        it.status = status
    }
}

interface ValidationPlanReferenceProjection: EntityReferenceProjection

fun ValidationPlanReferenceProjection.toEntityReference(): EntityReference<ValidationPlanId> {
    return EntityReference(ValidationPlanId(id), version)
}
