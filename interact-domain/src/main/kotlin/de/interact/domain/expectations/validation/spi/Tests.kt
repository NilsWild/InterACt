package de.interact.domain.expectations.validation.spi

import de.interact.domain.expectations.validation.`interface`.Interface
import de.interact.domain.expectations.validation.test.Test
import de.interact.domain.shared.TestId

interface Tests {
    fun find(testId: TestId): Test?
    fun findUnitTestsReceivingBy(nextInterface: Interface.IncomingInterface): Set<Test>
}