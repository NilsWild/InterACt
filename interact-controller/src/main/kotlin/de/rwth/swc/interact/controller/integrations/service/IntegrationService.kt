package de.rwth.swc.interact.controller.integrations.service

import de.rwth.swc.interact.controller.integrations.repository.IntegrationRepository
import de.rwth.swc.interact.domain.*
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class IntegrationService(private val integrationRepository: IntegrationRepository) {

    fun getIntegrationsForComponent(name: ComponentName, version: ComponentVersion): List<TestInvocationsDescriptor> {
        return integrationRepository.findReplacementsForComponent(name, version)
    }

    @Scheduled(fixedDelay = 20000)
    private fun deriveEnvironmentExpectations() {
        integrationRepository.deriveInteractionExpectations()
    }

    @Scheduled(fixedDelay = 30000)
    private fun findInteractionPathCandidates() {
        val expectations = integrationRepository.findUnvalidatedInteractionExpectationsWithoutPathCandidate()
        expectations.forEach {
            val pathInfo = integrationRepository.findNewInteractionExpectationPathCandidate(it)
            integrationRepository.setPathInfoForExpectation(it, pathInfo)
        }
    }

    fun updateInteractionExpectationInfo(interactionExpectationId: InteractionExpectationId, concreteTestCaseId: ConcreteTestCaseId, result: TestResult) {
        val interactionPath =
            integrationRepository.findInteractionExpectationPathInfoAndTestedPath(interactionExpectationId)
        integrationRepository.updateInterfaceExpectationInfo(
            interactionExpectationId,
            interactionPath.second.plus(concreteTestCaseId),
            if (result == TestResult.SUCCESS && interactionPath.first.interactionTests.size > interactionPath.second.size + 1)
                interactionPath.first.interactionTests[interactionPath.second.size + 1].testCaseId
            else null
        )
    }
}