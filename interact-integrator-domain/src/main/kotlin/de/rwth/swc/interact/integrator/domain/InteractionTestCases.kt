package de.rwth.swc.interact.integrator.domain

import java.util.*

data class InteractionTestCases(
    val interactionExpectation: UUID,
    val testCaseReference: TestCaseReference,
    val testCases: List<TestCase>
)