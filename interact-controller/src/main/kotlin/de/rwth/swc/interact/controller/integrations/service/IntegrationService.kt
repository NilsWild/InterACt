package de.rwth.swc.interact.controller.integrations.service

import de.rwth.swc.interact.controller.integrations.repository.IntegrationRepository
import de.rwth.swc.interact.controller.persistence.domain.ConcreteTestCase
import de.rwth.swc.interact.integrator.domain.InteractionTestCases
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.*

@Service
class IntegrationService(private val integrationRepository: IntegrationRepository) {

    fun getIntegrationsForComponent(name: String, version: String): List<InteractionTestCases> {
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

    fun updateInterfaceExpectationInfo(interactionExpectationId: UUID, concreteTestCaseId: UUID, result: ConcreteTestCase.TestResult) {
        val interactionPath =
            integrationRepository.findInteractionExpectationPathInfoAndTestedPath(interactionExpectationId)
        integrationRepository.updateInterfaceExpectationInfo(
            interactionExpectationId,
            interactionPath.second.plus(concreteTestCaseId),
            if (result == ConcreteTestCase.TestResult.SUCCESS && interactionPath.first.interactionTests.size > interactionPath.second.size + 1)
                interactionPath.first.interactionTests[interactionPath.second.size + 1].testCaseId
            else null
        )
    }
}