package de.interact.domain.expectations

import de.interact.domain.shared.AbstractTestId

data class TestDescriptor(
    val abstractTestCaseId: AbstractTestId,
    val parameters: List<TestParameter> = listOf()
)

