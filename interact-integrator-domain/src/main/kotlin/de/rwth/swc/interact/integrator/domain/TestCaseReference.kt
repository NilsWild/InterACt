package de.rwth.swc.interact.integrator.domain

data class TestCaseReference(
    val source: String,
    val abstractTestName: String,
    val concreteTestName: String,
    val parameters: List<String>
)