package de.interact.domain.expectations.validation.plan

import arrow.optics.copy
import de.interact.domain.expectations.validation.test.Test
import de.interact.domain.shared.Entity
import de.interact.domain.shared.EntityReference
import de.interact.domain.shared.InteractionExpectationId
import de.interact.domain.shared.ValidationPlanId
import java.util.*

sealed class ValidationPlan: Entity<ValidationPlanId>() {
    abstract val candidateFor: EntityReference<InteractionExpectationId>
    abstract val interactionGraph: InteractionGraph

    data class PendingValidationPlan(
        override val candidateFor: EntityReference<InteractionExpectationId>,
        override val interactionGraph: InteractionGraph,
        override val id: ValidationPlanId = ValidationPlanId(UUID.randomUUID()),
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
    if (interactionGraph.interactions.any { it is Interaction.Finished.Failed }) {
        return ValidationPlan.FailedValidationPlan(candidateFor, interactionGraph, id, version)
    } else if(interactionGraph.interactions.all { it is Interaction.Finished.Validated }) {
        return ValidationPlan.ValidatedValidationPlan(candidateFor, interactionGraph, id, version)
    } else {
        return this.copy(interactionGraph = interactionGraph)
    }
}