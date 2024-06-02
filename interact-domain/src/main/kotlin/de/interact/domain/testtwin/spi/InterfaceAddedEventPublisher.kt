package de.interact.domain.testtwin.spi

import de.interact.domain.testtwin.api.event.InterfaceAddedToVersionEvent

fun interface InterfaceAddedEventPublisher {
    fun publishNewInterface(newInterface: InterfaceAddedToVersionEvent)
}