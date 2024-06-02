package de.interact.controller.testexecution.repository

import java.util.*

interface TestCaseExecutionRequestProjection {
    val basedOn: AbstractTest
    val parameters: List<String>

    interface AbstractTest {
        val id: UUID
    }
}
