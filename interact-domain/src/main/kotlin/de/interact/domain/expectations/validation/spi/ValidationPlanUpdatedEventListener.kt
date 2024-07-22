package de.interact.domain.expectations.validation.spi

import de.interact.domain.expectations.validation.events.ValidationPlanUpdatedEvent

interface ValidationPlanUpdatedEventListener {
    fun onValidationPlanUpdated(event: ValidationPlanUpdatedEvent)
}