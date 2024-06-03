package de.interact.controller.expectations.specification

import de.interact.domain.expectations.derivation.events.InteractionExpectationAddedEvent
import de.interact.domain.expectations.derivation.spi.UnitTestBasedInteractionExpectationAddedEventListener
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class InteractionExpectationAddedEventListener(
    private val unitTestBasedInteractionExpectationAddedEventListeners: Set<UnitTestBasedInteractionExpectationAddedEventListener>
) {

    @Async
    @EventListener
    fun handleTestAddedEvent(event: InteractionExpectationAddedEvent) {
        when(event) {
            is InteractionExpectationAddedEvent.UnitTestBasedInteractionExpectationAddedEvent -> {
                unitTestBasedInteractionExpectationAddedEventListeners.forEach {
                    it.onUnitTestBasedInteractionExpectationAdded(event)
                }
            }
            is InteractionExpectationAddedEvent.SystemInteractionExpectationAddedEvent -> {
                TODO()
            }
        }
    }
}