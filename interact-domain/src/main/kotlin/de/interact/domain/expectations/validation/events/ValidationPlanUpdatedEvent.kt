package de.interact.domain.expectations.validation.events

import de.interact.domain.shared.ValidationPlanId

data class ValidationPlanUpdatedEvent (
    val validationPlanId: ValidationPlanId
)