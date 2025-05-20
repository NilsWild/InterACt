package de.interact.controller.testexecution.repository

import de.interact.controller.persistence.domain.EXECUTABLE_TEST_CASE_NODE_LABEL
import de.interact.controller.persistence.domain.TestCaseEntity
import de.interact.domain.shared.AbstractTestId
import de.interact.domain.testexecution.TestCaseParameter
import de.interact.domain.testexecution.TestInvocationDescriptor
import de.interact.domain.testexecution.spi.TestExecutionRequests
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import java.util.*

@Repository
interface TestExecutionRequestsRepository :
    org.springframework.data.repository.Repository<TestCaseEntity, UUID> {
    fun findTestInvocationDescriptorByDerivedFromTestVersionOfIdentifierAndDerivedFromTestIdentifier(
        componentName: String,
        componentVersion: String
    ): List<TestInvocationDescriptorProjection>
}

@Service
class TestExecutionRequestsDao(
    private val testExecutionRequestsRepository: TestExecutionRequestsRepository
) : TestExecutionRequests {
    override fun findInteractionTestsToExecuteForComponent(
        name: String,
        version: String
    ): List<TestInvocationDescriptor> {
        val projection =
            testExecutionRequestsRepository.findTestInvocationDescriptorByDerivedFromTestVersionOfIdentifierAndDerivedFromTestIdentifier(name, version).filter {
                it.labels.contains(EXECUTABLE_TEST_CASE_NODE_LABEL)
            }
        return projection.map {
            TestInvocationDescriptor(
                AbstractTestId(it.derivedFrom.id),
                it.parameters.map { TestCaseParameter(it) }
            )
        }.distinct()
    }
}