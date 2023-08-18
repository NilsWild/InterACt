package de.rwth.swc.interact.controller.integrations.service

import com.fasterxml.jackson.databind.ObjectMapper
import de.rwth.swc.interact.controller.integrations.dto.InteractionExpectationInfo
import de.rwth.swc.interact.controller.integrations.dto.InteractionPathInfo
import de.rwth.swc.interact.controller.integrations.dto.InteractionTestInfo
import de.rwth.swc.interact.controller.integrations.dto.PathStepInfo
import de.rwth.swc.interact.controller.integrations.repository.IntegrationRepository
import de.rwth.swc.interact.controller.persistence.service.*
import de.rwth.swc.interact.domain.*
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class IntegrationService(
    private val integrationRepository: IntegrationRepository,
    private val interactionExpectationDao: InteractionExpectationDao,
    private val concreteTestCaseDao: ConcreteTestCaseDao,
    private val outgoingInterfaceDao: OutgoingInterfaceDao,
    private val componentDao: ComponentDao,
    private val interactionExpectationValidationPlanDao: InteractionExpectationValidationPlanDao,
    private val mapper: ObjectMapper
) {

    fun findInteractionTestsToExecuteForComponent(
        name: ComponentName,
        version: ComponentVersion
    ): List<TestInvocationDescriptor> {

        val componentId = componentDao.findIdByNameAndVersion(name, version) ?: return emptyList()
        return integrationRepository.findInteractionTestsToExecuteForComponent(componentId)
    }

    @Scheduled(fixedDelay = 20000)
    fun deriveEnvironmentExpectations() {
        integrationRepository.deriveInteractionExpectations()
    }

    @Scheduled(fixedDelay = 30000)
    fun findInteractionPathCandidates() {
        val expectations = integrationRepository.findByMissingValidationCandidate()
        expectations.forEach {
            val validationCandidates = findNewInteractionExpectationPathCandidates(it)
            createValidationCandidatesForExpectationIfTheyDoNotExist(it.id, validationCandidates)
        }
    }

    private fun createValidationCandidatesForExpectationIfTheyDoNotExist(
        interactionExpectationId: InteractionExpectationId,
        validationCandidates: Collection<InteractionPathInfo>
    ) {
        validationCandidates.filter {
            !interactionExpectationValidationPlanDao.existsByPathInfo(
                mapper.writeValueAsString(
                    it
                )
            )
        }.forEach {
            val validationPlanId = interactionExpectationValidationPlanDao.save(
                InteractionExpectationValidationPlan(
                    mapper.writeValueAsString(it),
                    integrationRepository.determineTestInvocationDescriptor(
                        it.interactionTests[1],
                        listOf(it.interactionTests[0].testCaseId)
                    ),
                    null,
                    listOf(it.interactionTests[0].testCaseId)
                )
            )
            interactionExpectationDao.addValidationPlan(interactionExpectationId, validationPlanId)
            tryToValidateNextInteraction(validationPlanId)
        }
    }

    private fun findNewInteractionExpectationPathCandidates(interactionExpectationInfo: InteractionExpectationInfo): Collection<InteractionPathInfo> {
        val results = mutableListOf<InteractionPathInfo>()
        val startingTestCaseId = concreteTestCaseDao.findIdByTriggeredMessage(interactionExpectationInfo.fromId)!!
        //TODO("consider multiple tos")
        val terminalNodes = integrationRepository.findTerminalNodesForTargetMessage(interactionExpectationInfo.toIds[0])
        val queue = LinkedList<InteractionPathInfo>()
        val currentInteractionPath = mutableListOf<InteractionTestInfo>()
        currentInteractionPath.add(
            InteractionTestInfo(
                startingTestCaseId,
                interactionExpectationInfo.fromId,
                mapOf()
            )
        )
        queue.offer(InteractionPathInfo(currentInteractionPath))
        while (!queue.isEmpty()) {
            val searchPath = queue.poll()
            val pathEnd = searchPath.interactionTests[searchPath.interactionTests.size - 1].nextStart
            if (terminalNodes.contains(pathEnd)) {
                //füge den basis case mit der finalen ersetzung zu der liste der integrationstests hinzu
                val replacementMessageInterfaceId = outgoingInterfaceDao.findByMessage(pathEnd!!)!!

                val newTestCaseReplacements = searchPath.testCaseReplacements.getOrElse(startingTestCaseId) {
                    mapOf()
                }.plus(
                    Pair(
                        //TODO("consider multiple tos")
                        interactionExpectationInfo.toIds[0],
                        replacementMessageInterfaceId
                    )
                )

                val newReplacements = searchPath.testCaseReplacements.toMutableMap()
                newReplacements[startingTestCaseId] = newTestCaseReplacements

                results.add(
                    InteractionPathInfo(
                        searchPath.interactionTests.plus(
                            InteractionTestInfo(
                                startingTestCaseId,
                                null,
                                newReplacements[startingTestCaseId]!!
                            )
                        ),
                        searchPath.visitedInterfaces.plus(replacementMessageInterfaceId),
                        searchPath.visitedComponentTestCase.toMap(),
                        newReplacements.toMap()
                    )
                )
            }
            val nextInteractionStep = integrationRepository.expandPathFrom(pathEnd!!)
            for (n in nextInteractionStep) {
                if (isNotVisited(n, searchPath)) {
                    var newReplacements: Map<ConcreteTestCaseId, Map<MessageId, InterfaceId>>
                    if (searchPath.testCaseReplacements[n.testCase] == null) {
                        newReplacements = searchPath.testCaseReplacements.plus(
                            Pair(
                                n.testCase,
                                mapOf(Pair(n.originalMessage, n.replacementMessageInterface))
                            )
                        )
                    } else {
                        val newTestCaseReplacements = searchPath.testCaseReplacements[n.testCase]!!.plus(
                            Pair(
                                n.originalMessage,
                                n.replacementMessageInterface
                            )
                        )
                        newReplacements = searchPath.testCaseReplacements.toMutableMap()
                        newReplacements.replace(n.testCase, newTestCaseReplacements)
                    }
                    val ip = InteractionPathInfo(
                        searchPath.interactionTests.plus(
                            InteractionTestInfo(
                                n.testCase,
                                n.next,
                                newReplacements[n.testCase]!!
                            )
                        ),
                        searchPath.visitedInterfaces.plus(n.originalMessageInterface),
                        if (searchPath.visitedComponentTestCase[n.component] == null) {
                            searchPath.visitedComponentTestCase.plus(Pair(n.component, n.testCase))
                        } else {
                            searchPath.visitedComponentTestCase
                        },
                        newReplacements
                    )
                    queue.offer(ip)
                }
            }
        }
        return results
    }

    private fun isNotVisited(stepInfo: PathStepInfo, searchPath: InteractionPathInfo): Boolean {
        return !searchPath.visitedInterfaces.contains(stepInfo.originalMessageInterface) &&
                (searchPath.visitedComponentTestCase[stepInfo.component] == null
                        || searchPath.visitedComponentTestCase[stepInfo.component] == stepInfo.testCase)
    }

    fun updateInteractionExpectationValidationPlanInfos(concreteTestCase: ConcreteTestCase) {
        val atc = integrationRepository.findAbstractTestCaseByConcreteTestCaseId(concreteTestCase.id!!)
        val testInvocationDescriptor = TestInvocationDescriptor(
            atc,
            concreteTestCase.parameters.map { MessageValue(it.value) }
        )
        val validationPlans =
            interactionExpectationValidationPlanDao.findByTestInvocationDescriptor(testInvocationDescriptor)
        validationPlans.forEach {
            integrationRepository.updateInterfaceExpectationValidationPlanWithNewExecution(
                it,
                concreteTestCase
            )
            tryToValidateNextInteraction(it.id!!)
        }
    }

    private fun tryToValidateNextInteraction(validationPlanId: InteractionExpectationValidationPlanId) {
        val validationPlan = interactionExpectationValidationPlanDao.findById(validationPlanId)
        if (validationPlan.nextTest != null) {
            val concreteTestCase = concreteTestCaseDao.findByAbstractTestCaseIdAndParameters(
                validationPlan.nextTest!!.abstractTestCase.id!!,
                validationPlan.nextTest!!.testInvocations.map { TestCaseParameter(it.value) }
            )
            if (concreteTestCase != null) {
                integrationRepository.updateInterfaceExpectationValidationPlanWithNewExecution(
                    validationPlan,
                    concreteTestCase
                )
                tryToValidateNextInteraction(validationPlanId)
            } else {
                interactionExpectationValidationPlanDao.setNextComponent(
                    validationPlanId,
                    integrationRepository.findComponentForAbstractTestCaseId(validationPlan.nextTest!!.abstractTestCase.id!!)
                )
            }
        }
    }
}