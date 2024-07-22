package de.interact.controller.expectations.validation

import de.interact.domain.expectations.validation.events.ValidationPlanUpdatedEvent
import de.interact.domain.expectations.validation.spi.ValidationPlanUpdatedEventListener
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class SpringValidationPlanUpdatedEventListener (
    private val validationPlanUpdatedEventListeners: Set<ValidationPlanUpdatedEventListener>
){
    @Async
    @EventListener
    fun handleTestAddedEvent(event: ValidationPlanUpdatedEvent) {
        validationPlanUpdatedEventListeners.forEach {
            it.onValidationPlanUpdated(event)
        }
    }
}