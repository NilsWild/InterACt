package de.interact.domain.testtwin.api.event

import de.interact.domain.shared.EntityReference
import de.interact.domain.shared.InteractionTestId
import de.interact.domain.shared.TestId
import de.interact.domain.shared.UnitTestId

sealed interface TestAddedEvent {
    val test: EntityReference<TestId>
}

data class UnitTestAddedEvent(
    override val test: EntityReference<UnitTestId>
) : TestAddedEvent

data class InteractionTestAddedEvent(
    override val test: EntityReference<InteractionTestId>
) : TestAddedEvent