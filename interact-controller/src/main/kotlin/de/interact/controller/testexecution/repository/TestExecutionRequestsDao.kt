package de.interact.controller.testexecution.repository

import de.interact.controller.persistence.domain.TestCaseExecutionRequestEntity
import de.interact.domain.shared.AbstractTestId
import de.interact.domain.testexecution.TestCaseParameter
import de.interact.domain.testexecution.TestInvocationDescriptor
import de.interact.domain.testexecution.spi.TestExecutionRequests
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import java.util.*

@Repository
interface TestExecutionRequestsRepository :
    org.springframework.data.repository.Repository<TestCaseExecutionRequestEntity, UUID> {
    fun findByBasedOnTestVersionOfIdentifierAndBasedOnTestIdentifier(
        componentName: String,
        componentVersion: String
    ): List<TestCaseExecutionRequestProjection>
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
            testExecutionRequestsRepository.findByBasedOnTestVersionOfIdentifierAndBasedOnTestIdentifier(name, version)
        return projection.map {
            TestInvocationDescriptor(
                AbstractTestId(it.basedOn.id),
                it.parameters.map { TestCaseParameter(it) }
            )
        }
    }
}