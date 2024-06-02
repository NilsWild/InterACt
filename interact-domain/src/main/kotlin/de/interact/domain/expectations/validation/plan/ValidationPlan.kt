package de.interact.domain.expectations.validation.plan

import de.interact.domain.expectations.validation.test.Test
import de.interact.domain.shared.Entity
import de.interact.domain.shared.EntityReference
import de.interact.domain.shared.InteractionExpectationId
import de.interact.domain.shared.ValidationPlanId

sealed class ValidationPlan: Entity<ValidationPlanId>() {
    abstract val candidateFor: EntityReference<InteractionExpectationId>
    abstract val interactionGraph: InteractionGraph

    data class PendingValidationPlan(
        override val candidateFor: EntityReference<InteractionExpectationId>,
        override val interactionGraph: InteractionGraph,
        override val id: ValidationPlanId,
        override val version: Long? = null
    ) : ValidationPlan()

    data class ValidatedValidationPlan(
        override val candidateFor: EntityReference<InteractionExpectationId>,
        override val interactionGraph: InteractionGraph,
        override val id: ValidationPlanId,
        override val version: Long? = null
    ) : ValidationPlan()

    data class FailedValidationPlan(
        override val candidateFor: EntityReference<InteractionExpectationId>,
        override val interactionGraph: InteractionGraph,
        override val id: ValidationPlanId,
        override val version: Long? = null
    ) : ValidationPlan()

}

fun ValidationPlan.PendingValidationPlan.handle(test: Test): ValidationPlan {
    val interactionGraph = interactionGraph.handle(test)
    return this.copy(interactionGraph = interactionGraph)
}