package de.interact.domain.testexecution

import de.interact.domain.shared.AbstractTestId

data class TestInvocationDescriptor(
    val abstractTestId: AbstractTestId,
    val parameters: List<TestCaseParameter>
)