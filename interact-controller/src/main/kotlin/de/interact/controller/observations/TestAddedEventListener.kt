package de.interact.controller.observations

import de.interact.domain.testtwin.api.event.InteractionTestAddedEvent
import de.interact.domain.testtwin.api.event.TestAddedEvent
import de.interact.domain.testtwin.api.event.UnitTestAddedEvent
import de.interact.domain.testtwin.spi.InteractionTestAddedEventListener
import de.interact.domain.testtwin.spi.UnitTestAddedEventListener
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class TestAddedEventListener(
    private val unitTestAddedEventListeners: Set<UnitTestAddedEventListener>,
    private val interactionTestAddedEventListeners: Set<InteractionTestAddedEventListener>
) {

    @Async
    @EventListener
    fun handleTestAddedEvent(event: TestAddedEvent) {
        when(event) {
            is UnitTestAddedEvent -> {
                unitTestAddedEventListeners.forEach {
                    it.onUnitTestCaseAdded(event)
                }
            }
            is InteractionTestAddedEvent -> {
                interactionTestAddedEventListeners.forEach {
                    it.onInteractionTestAdded(event)
                }
            }
        }
    }
}