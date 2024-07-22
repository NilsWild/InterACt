package de.interact.domain.expectations.validation.api

import de.interact.domain.expectations.validation.events.ValidationPlanUpdatedEvent
import de.interact.domain.expectations.validation.plan.ValidationPlan
import de.interact.domain.expectations.validation.spi.UnitTestBasedInteractionExpectations
import de.interact.domain.expectations.validation.spi.ValidationPlanUpdatedEventListener
import de.interact.domain.expectations.validation.spi.ValidationPlans
import de.interact.domain.shared.InteractionExpectationStatus

class InteractionExpectationsManager(
    private val validationPlans: ValidationPlans,
    private val unitTestBasedInteractionExpectations: UnitTestBasedInteractionExpectations
): ValidationPlanUpdatedEventListener {
    override fun onValidationPlanUpdated(event: ValidationPlanUpdatedEvent) {
        val validationPlan = validationPlans.find(event.validationPlanId)
        val interactionExpectation = unitTestBasedInteractionExpectations.findByValidationPlansId(event.validationPlanId)
        if(validationPlan is ValidationPlan.ValidatedValidationPlan) {
            unitTestBasedInteractionExpectations.setStatus(interactionExpectation!!.id, InteractionExpectationStatus.Validated)
        } else {
            val validationPlansForInteraction = validationPlans.findByInteractionExpectationId(interactionExpectation!!.id)
            if(validationPlansForInteraction.any{ it is ValidationPlan.ValidatedValidationPlan }) {
                unitTestBasedInteractionExpectations.setStatus(interactionExpectation.id, InteractionExpectationStatus.Validated)
            } else if(validationPlansForInteraction.any{ it is ValidationPlan.PendingValidationPlan }){
                unitTestBasedInteractionExpectations.setStatus(interactionExpectation.id, InteractionExpectationStatus.Validating)
            } else {
                unitTestBasedInteractionExpectations.setStatus(interactionExpectation.id, InteractionExpectationStatus.NoValidatingPathFound)
            }
        }
    }
}