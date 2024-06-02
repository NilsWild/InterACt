package de.interact.domain.testtwin.spi

import de.interact.domain.testtwin.api.event.InteractionTestAddedEvent

interface InteractionTestAddedEventListener {
    fun onInteractionTestAdded(event: InteractionTestAddedEvent)
}