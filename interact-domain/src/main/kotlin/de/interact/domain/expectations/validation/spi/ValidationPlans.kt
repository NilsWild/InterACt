package de.interact.domain.expectations.validation.spi

import de.interact.domain.expectations.validation.plan.ValidationPlan
import de.interact.domain.expectations.validation.test.Test
import de.interact.domain.shared.UnitTestBasedInteractionExpectationId
import de.interact.domain.shared.ValidationPlanId

interface ValidationPlans {
    fun find(id: ValidationPlanId): ValidationPlan?
    fun waitingFor(test: Test): Set<ValidationPlan.PendingValidationPlan>
    fun save(validationPlan: ValidationPlan): ValidationPlan
    fun findByInteractionExpectationId(id: UnitTestBasedInteractionExpectationId): List<ValidationPlan>
}