package de.interact.domain.expectations.validation.spi

import de.interact.domain.expectations.validation.plan.ValidationPlan
import de.interact.domain.expectations.validation.test.Test

interface ValidationPlans {
    fun waitingFor(test: Test): Set<ValidationPlan.PendingValidationPlan>
    fun save(validationPlan: ValidationPlan): ValidationPlan
}