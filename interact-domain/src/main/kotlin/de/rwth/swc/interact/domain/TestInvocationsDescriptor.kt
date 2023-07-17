package de.rwth.swc.interact.domain

data class TestInvocationsDescriptor(
    val abstractTestCase: AbstractTestCase,
    val testInvocations: List<TestInvocationDescriptor> = listOf()
)

