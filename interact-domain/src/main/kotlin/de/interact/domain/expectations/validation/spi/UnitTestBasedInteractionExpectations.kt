package de.interact.domain.expectations.validation.spi

import de.interact.domain.expectations.validation.interactionexpectation.InteractionExpectation
import de.interact.domain.shared.EntityReference
import de.interact.domain.shared.UnitTestBasedInteractionExpectationId
import de.interact.domain.shared.UnitTestId

interface UnitTestBasedInteractionExpectations {
    fun find(id: UnitTestBasedInteractionExpectationId): InteractionExpectation.UnitTestBasedInteractionExpectation?
    fun findInteractionExpectationsPotentiallyDependantOn(test: EntityReference<UnitTestId>): Set<EntityReference<UnitTestBasedInteractionExpectationId>>
}