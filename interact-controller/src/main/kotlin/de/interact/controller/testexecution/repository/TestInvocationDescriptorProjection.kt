package de.interact.controller.testexecution.repository

import de.interact.controller.persistence.domain.AbstractTestCaseReferenceProjection

interface TestInvocationDescriptorProjection {
    val derivedFrom: AbstractTestCaseReferenceProjection
    val labels: List<String>
    val parameters: List<String>
}
