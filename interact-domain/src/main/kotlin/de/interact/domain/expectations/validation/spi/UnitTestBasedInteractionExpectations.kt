package de.interact.domain.expectations.validation.spi

import de.interact.domain.expectations.validation.interactionexpectation.InteractionExpectation
import de.interact.domain.shared.*

interface UnitTestBasedInteractionExpectations {
    fun find(id: UnitTestBasedInteractionExpectationId): InteractionExpectation.UnitTestBasedInteractionExpectation?
    fun findInteractionExpectationsPotentiallyDependantOn(test: EntityReference<UnitTestId>): Set<EntityReference<UnitTestBasedInteractionExpectationId>>
    fun findByValidationPlansId(id: ValidationPlanId): InteractionExpectation.UnitTestBasedInteractionExpectation?
    fun setStatus(id: UnitTestBasedInteractionExpectationId, validated: InteractionExpectationStatus)
}