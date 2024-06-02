package de.interact.domain.expectations.derivation.spi

import de.interact.domain.expectations.derivation.interaction.Interaction
import de.interact.domain.shared.TestId

interface Interactions {
    fun findForTest(testId: TestId): Set<Interaction>
}