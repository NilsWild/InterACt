package de.interact.domain.expectations.validation.spi

import de.interact.domain.expectations.validation.interactionexpectation.InteractionExpectation
import de.interact.domain.shared.UnitTestBasedInteractionExpectationId

interface UnitTestBasedInteractionExpectations {
    fun find(id: UnitTestBasedInteractionExpectationId): InteractionExpectation.UnitTestBasedInteractionExpectation?
}