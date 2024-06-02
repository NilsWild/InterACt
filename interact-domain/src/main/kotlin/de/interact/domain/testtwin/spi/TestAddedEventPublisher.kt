package de.interact.domain.testtwin.spi

import de.interact.domain.testtwin.api.event.InteractionTestAddedEvent
import de.interact.domain.testtwin.api.event.UnitTestAddedEvent

interface TestAddedEventPublisher {
    fun publishNewUnitTest(newUnitTest: UnitTestAddedEvent)
    fun publishNewInteractionTest(newInteractionTest: InteractionTestAddedEvent)
}