package de.interact.domain.expectations.validation.api

import de.interact.domain.expectations.TestParameter
import de.interact.domain.expectations.derivation.events.InteractionExpectationAddedEvent
import de.interact.domain.expectations.derivation.spi.UnitTestBasedInteractionExpectationAddedEventListener
import de.interact.domain.expectations.validation.plan.*
import de.interact.domain.expectations.validation.spi.Interfaces
import de.interact.domain.expectations.validation.spi.Tests
import de.interact.domain.expectations.validation.spi.UnitTestBasedInteractionExpectations
import de.interact.domain.expectations.validation.spi.ValidationPlans
import de.interact.domain.expectations.validation.test.ComponentResponseMessage
import de.interact.domain.expectations.validation.test.Message
import de.interact.domain.expectations.validation.test.StimulusMessage
import de.interact.domain.expectations.validation.test.Test
import de.interact.domain.shared.*
import de.interact.domain.testtwin.api.event.InteractionTestAddedEvent
import de.interact.domain.testtwin.api.event.UnitTestAddedEvent
import de.interact.domain.testtwin.spi.InteractionTestAddedEventListener
import de.interact.domain.testtwin.spi.UnitTestAddedEventListener
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentHashMap.KeySetView

class ValidationPlansManager(
    private val tests: Tests,
    private val validationPlans: ValidationPlans,
    private val unitTestBasedInteractionExpectations: UnitTestBasedInteractionExpectations,
    private val interfaces: Interfaces
): UnitTestAddedEventListener, InteractionTestAddedEventListener, UnitTestBasedInteractionExpectationAddedEventListener {

    private val interactionExpectationLocks: KeySetView<InteractionExpectationId, Boolean> = ConcurrentHashMap.newKeySet()

    override fun onUnitTestCaseAdded(event: UnitTestAddedEvent) {
        val potentiallyDependantInteractionExpectations = unitTestBasedInteractionExpectations.findInteractionExpectationsPotentiallyDependantOn(event.test)
        potentiallyDependantInteractionExpectations.forEach { deriveValidationPlansForUnitTestBaseInteractionExpectation(it) }
        processTest(event.test)
    }

    override fun onInteractionTestAdded(event: InteractionTestAddedEvent) {
        processTest(event.test)
    }

    private fun processTest(testReference: EntityReference<TestId>) {
        val test = tests.find(testReference.id)!!
        val dependantValidationPlans = validationPlans.waitingFor(test)
        var updatedValidationPlans = dependantValidationPlans.map {
            var updatedPlan = it.handle(test)
            if(updatedPlan is ValidationPlan.PendingValidationPlan) {
                updatedPlan = makePendingInteractionsExecutable(updatedPlan)
            }
            updatedPlan
        }
        updatedValidationPlans = updatedValidationPlans.map { plan ->
            progressValidationPlan(plan)
        }
        updatedValidationPlans.forEach(validationPlans::save)
    }

    override fun onUnitTestBasedInteractionExpectationAdded(
        event: InteractionExpectationAddedEvent.UnitTestBasedInteractionExpectationAddedEvent
    ) {
        deriveValidationPlansForUnitTestBaseInteractionExpectation(event.interactionExpectation)
    }

    private fun deriveValidationPlansForUnitTestBaseInteractionExpectation(interactionExpectationReference: EntityReference<UnitTestBasedInteractionExpectationId>) {
        try {
            interactionExpectationLocks.add(interactionExpectationReference.id)

            val interactionExpectation = unitTestBasedInteractionExpectations.find(interactionExpectationReference.id)!!
            val derivedFromTest = tests.find(interactionExpectation.derivedFrom.id)!!
            val interactionGraphs = mutableSetOf<InteractionGraph>()
            val interactionGraphsToExpand = LinkedList<InteractionGraph>()

            interactionGraphsToExpand.add(
                InteractionGraph().addInteraction(
                    Interaction.Finished.Validated(
                        EntityReference(UnitTestId(derivedFromTest.id.value), derivedFromTest.version),
                        TestCase.CompleteTestCase.Succeeded(
                            derivedFromTest.derivedFrom,
                            emptySet(),
                            derivedFromTest.parameters,
                            interactionExpectation.derivedFrom
                        ),
                        derivedFromTest.triggeredMessages.filterIsInstance<StimulusMessage>().firstOrNull()?.let { setOf(it.receivedBy)} ?: emptySet(),
                        setOf(
                            derivedFromTest.triggeredMessages
                                .filterIsInstance<ComponentResponseMessage>()
                                .first{interactionExpectation.expectFrom.id == it.id}.sentBy
                            //TODO expectFrom message is crucial for next replacement
                        )
                    )
                )
            )

            while (interactionGraphsToExpand.isNotEmpty()) {
                val interactionGraphToExpand = interactionGraphsToExpand.poll()
                val expandedInteractionGraphs = expandInteractionGraph(interactionGraphToExpand)
                expandedInteractionGraphs.forEach { interactionGraph ->
                    if (interactionGraph.leadsTo(interactionExpectation.expectTo.map { it.id }.toSet())) {
                        interactionGraphs.add(interactionGraph.removeUnnecessaryInteractionsToReach(interactionExpectation.expectTo.map { it.id }.toSet()))
                    } else if (interactionGraph != interactionGraphToExpand) {
                        interactionGraphsToExpand.offer(interactionGraph)
                    }
                }
            }

            val existingValidationPlans = validationPlans.findByInteractionExpectationId(interactionExpectationReference.id)
            existingValidationPlans.map { it.interactionGraph.id }.forEach { exiting -> interactionGraphs.filter { it.id == exiting }.forEach { interactionGraphs.remove(it) }}

            val validationPlansToStore = interactionGraphs.map { interactionGraph ->
                val validationPlan = ValidationPlan.PendingValidationPlan(
                    interactionExpectationReference,
                    interactionGraph
                )
                progressValidationPlan(validationPlan)
            }

            validationPlans.save(
                validationPlansToStore
            )
        } finally {
            interactionExpectationLocks.remove(interactionExpectationReference.id)
        }
    }

    private fun progressValidationPlan(plan: ValidationPlan): ValidationPlan {
        var preUpdatedPlan: ValidationPlan
        var updatedPlan = plan
        do {
            preUpdatedPlan = updatedPlan
            if (updatedPlan is ValidationPlan.PendingValidationPlan) {
                updatedPlan = validateExecutableInteractionsIfAlreadyExecuted(plan as ValidationPlan.PendingValidationPlan)
                if (updatedPlan is ValidationPlan.PendingValidationPlan) {
                    updatedPlan = makePendingInteractionsExecutable(updatedPlan)
                }
            }
        } while (updatedPlan != preUpdatedPlan)
        return updatedPlan
    }

    private fun validateExecutableInteractionsIfAlreadyExecuted(plan: ValidationPlan.PendingValidationPlan): ValidationPlan {
        var updatedPlan: ValidationPlan = plan
        val executableTestCases = plan.interactionGraph.interactions.filterIsInstance<Interaction.Executable>()
        val alreadyExecutedTests = executableTestCases.map {
            tests.findTestByDerivedFromAndParameters(it.testCase.derivedFrom.id, it.testCase.parameters)
        }.filterNotNull()
        for(executedTest in alreadyExecutedTests) {
            if(updatedPlan is ValidationPlan.PendingValidationPlan) {
                updatedPlan = updatedPlan.handle(executedTest)
            }else {
                break
            }
        }
        return updatedPlan
    }


    private fun expandInteractionGraph(interactionGraph: InteractionGraph): List<InteractionGraph> {
        val expansions = interactionGraph.sinks.map { sink ->
            val sinkExpansions = expandFromSink(interactionGraph, sink)
            sink to sinkExpansions
        }
        var expandedInteractionGraphs = listOf(interactionGraph)
        expansions.forEach { (sink, interfaceExpansions) ->
            interfaceExpansions.forEach { (_, segments) ->
                val nextSegmentGroups = getSegmentSubsets(segments)
                expandedInteractionGraphs = nextSegmentGroups.flatMap { groupSegments ->
                    expandedInteractionGraphs.map { graph ->
                        var updatedGraph = graph
                        groupSegments.forEach { segment ->
                            updatedGraph = updatedGraph.addInteraction(segment, setOf(sink))
                        }
                        updatedGraph.replaceInteractions(updatedGraph.interactions.map { it to it.clone() }.toMap())
                    }
                }
            }
        }
        return expandedInteractionGraphs
    }

    private fun expandFromSink(
        interactionGraph: InteractionGraph,
        sink: Interaction
    ): Map<IncomingInterfaceId, List<Interaction>> {

        val result = mutableMapOf<IncomingInterfaceId, List<Interaction>>()

        sink.to.forEach { startInterface ->
            val boundInterfaces = interfaces.findIncomingInterfacesBoundToOutgoingInterface(startInterface.id)
            boundInterfaces.forEach { nextInterface ->
                val unitTests = tests.findUnitTestsReceivingBy(nextInterface)
                val interfaceCount = interactionGraph.findAllInteractionTraversingReverseAdjacencyMap(sink) {
                    it.testCase.replacements.any { it.messageToReplace.interfaceReference.id == nextInterface.id }
                }.count()
                val nextSegments = unitTests.filter { nextTestCandidate ->
                    interactionGraph.findFirstInteractionTraversingReverseAdjacencyMap(sink) {
                    // we can continue if the next interaction is derived from the same concrete test case if
                    // it's derived from the same abstract test case as a previous interaction
                    (it.testCase.derivedFrom.id == nextTestCandidate.derivedFrom.id && it.derivedFrom.id != nextTestCandidate.id)
                            // and it needs to be derived from the same abstract testcase if it's testing the same component
                            || nextTestCandidate.testFor.id == tests.find(it.derivedFrom.id)!!.testFor.id && it.testCase.derivedFrom.id != nextTestCandidate.derivedFrom.id
                    } == null
                }.map { unitTest ->
                    Interaction.Pending(
                        EntityReference(UnitTestId(unitTest.id.value), unitTest.version),
                        TestCase.IncompleteTestCase(
                            unitTest.derivedFrom,
                            setOf(
                                //TODO multiple replacements so received message needs to be filtered by interface
                                Replacement(
                                    MessageToReplaceIdentifier(
                                        unitTest.triggeredMessages.filterIsInstance<Message.ReceivedMessage>()
                                            .filter { it.receivedBy.id == nextInterface.id }[interfaceCount].toEntityReference(),
                                        nextInterface.toEntityReference()
                                    ),
                                    ReplacementIdentifier(
                                        startInterface
                                    )
                                )
                            )
                        ),
                        setOf(nextInterface.toEntityReference()),
                        unitTest.triggeredMessages.filterIsInstance<ComponentResponseMessage>()
                            .filter {
                                it.dependsOn.map { it.id }.contains(
                                    unitTest.triggeredMessages.filterIsInstance<Message.ReceivedMessage>()
                                        .first { it.receivedBy.id == nextInterface.id }.id
                                )
                            }.map { it.sentBy }.toSet()
                        //TODO same thing different shit, ändern das message gespeichert wird und in der graphql api mappen auf interface
                    )
                }
                result[nextInterface.id] = nextSegments
            }
        }
        return result
    }

    private fun getSegmentSubsets(segments: List<Interaction>): List<List<Interaction>> {
        val groupedSegments = segments.groupingBy { it.from }
        if (!groupedSegments.eachCount().any { it.value > 1 }) {
            return listOf(segments)
        }
        var subsets = listOf(segments)
        groupedSegments.eachCount().filter { it.value > 1 }.forEach { (from, _) ->
            val segmentsForTestId = segments.filter { it.from == from }
            subsets = subsets.flatMap { set ->
                segmentsForTestId.map { segment ->
                    listOf(segment, *set.filter { !segmentsForTestId.contains(it) }.toTypedArray())
                }
            }
        }
        return subsets
    }

    private fun makePendingInteractionsExecutable(validationPlan: ValidationPlan.PendingValidationPlan): ValidationPlan.PendingValidationPlan {
        val interactionGraph = validationPlan.interactionGraph
        val nextInteractionsToTest = interactionGraph.interactions.filterIsInstance<Interaction.Finished.Validated>().flatMap {
            interactionGraph.adjacencyMap[it]!!
        }.filterIsInstance<Interaction.Pending>().filter {
            interactionGraph.reverseAdjacencyMap[it]!!.all { it is Interaction.Finished.Validated }
        }

        return if (nextInteractionsToTest.isEmpty()) {
            validationPlan
        } else {
            val replacements = interactionGraph.interactions.map {
                if(it is Interaction.Pending && nextInteractionsToTest.contains(it)) {
                    val lastManipulation = interactionGraph.findFirstInteractionTraversingReverseAdjacencyMap(it){ interaction ->
                        interaction is Interaction.Finished.Validated && interaction.derivedFrom == it.derivedFrom
                    }

                    val testCaseToManipulate = if(lastManipulation != null) {
                        tests.find(lastManipulation.derivedFrom.id)!!
                    } else {
                        tests.find(it.derivedFrom.id)!!
                    }
                    val replacements = extractReplacements(
                        interactionGraph.reverseAdjacencyMap[it]!!.map {
                            val casted = (it as Interaction.Finished.Validated)
                            casted
                        }.toSet(),
                        it.testCase.replacements
                    )
                    val parameters = extractNewParameters(testCaseToManipulate, replacements)
                    it to Interaction.Executable(
                        it.derivedFrom,
                        TestCase.ExecutableTestCase(
                            it.testCase.derivedFrom,
                            it.testCase.replacements,
                            parameters,
                            it.testCase.id,
                            it.testCase.version
                        ),
                        it.from,
                        it.to,
                        it.id,
                        it.version
                    )
                } else {
                    it to it
                }
            }.toMap()
            validationPlan.copy(interactionGraph = interactionGraph.replaceInteractions(replacements))
        }
    }

    private fun extractNewParameters(
        testCaseToManipulate: Test,
        replacements: Map<EntityReference<IncomingInterfaceId>, ComponentResponseMessage>
    ): List<TestParameter> {
        return testCaseToManipulate.triggeredMessages.filterIsInstance<Message.ReceivedMessage>().map {
            if(replacements.containsKey(it.receivedBy)) {
                TestParameter(replacements[it.receivedBy]!!.value.toString())
            } else {
                TestParameter(it.value.toString())
            }
        }.toList()
    }

    private fun extractReplacements(interactions: Set<Interaction.Finished.Validated>, replacements: Set<Replacement>): Map<EntityReference<IncomingInterfaceId>, ComponentResponseMessage> {
        return replacements.map { replacement ->
            val testCaseToCopyFrom = interactions.first { it.to.contains(replacement.replacement.interfaceToCopyFrom) }.testCase.actualTest
            val testToCopyFrom = tests.find(testCaseToCopyFrom.id)!!
            val replacementMessage = testToCopyFrom.triggeredMessages.filterIsInstance<ComponentResponseMessage>().first {
                it.sentBy == replacement.replacement.interfaceToCopyFrom
            }
            replacement.messageToReplace.interfaceReference to replacementMessage
        }.toMap()
    }
}