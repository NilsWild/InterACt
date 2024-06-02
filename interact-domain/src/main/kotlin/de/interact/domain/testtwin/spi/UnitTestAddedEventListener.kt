package de.interact.domain.testtwin.spi

import de.interact.domain.testtwin.api.event.UnitTestAddedEvent

interface UnitTestAddedEventListener {
    fun onUnitTestCaseAdded(event: UnitTestAddedEvent)
}