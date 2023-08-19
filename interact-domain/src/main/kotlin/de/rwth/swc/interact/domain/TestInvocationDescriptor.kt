package de.rwth.swc.interact.domain

data class TestInvocationDescriptor(
    val abstractTestCase: AbstractTestCase,
    val parameters: List<MessageValue> = listOf()
)

