package de.interact.domain.expectations.derivation.spi

import de.interact.domain.expectations.derivation.interactionexpectation.InteractionExpectation
import de.interact.domain.shared.SystemInteractionExpectationId
import de.interact.domain.shared.UnitTestBasedInteractionExpectationId

interface UnitTestBasedInteractionExpectations {
    fun save(expectation: InteractionExpectation.UnitTestBasedInteractionExpectation): InteractionExpectation.UnitTestBasedInteractionExpectation
    fun find(expectationId: UnitTestBasedInteractionExpectationId): InteractionExpectation.UnitTestBasedInteractionExpectation?
}

interface SystemInteractionExpectations {
    fun save(expectation: InteractionExpectation.SystemInteractionExpectation): InteractionExpectation.SystemInteractionExpectation
    fun find(expectationId: SystemInteractionExpectationId): InteractionExpectation.SystemInteractionExpectation?
}