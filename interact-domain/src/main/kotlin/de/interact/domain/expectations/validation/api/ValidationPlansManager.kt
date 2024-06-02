package de.interact.domain.expectations.validation.api

import arrow.fx.coroutines.unit
import de.interact.domain.expectations.derivation.events.InteractionExpectationAddedEvent
import de.interact.domain.expectations.derivation.spi.UnitTestBasedInteractionExpectationAddedEventListener
import de.interact.domain.expectations.validation.`interface`.toEntityReference
import de.interact.domain.expectations.validation.plan.*
import de.interact.domain.expectations.validation.spi.Interfaces
import de.interact.domain.expectations.validation.spi.Tests
import de.interact.domain.expectations.validation.spi.UnitTestBasedInteractionExpectations
import de.interact.domain.expectations.validation.spi.ValidationPlans
import de.interact.domain.expectations.validation.test.ComponentResponseMessage
import de.interact.domain.expectations.validation.test.Message
import de.interact.domain.expectations.validation.test.StimulusMessage
import de.interact.domain.expectations.validation.test.toEntityReference
import de.interact.domain.shared.EntityReference
import de.interact.domain.shared.IncomingInterfaceId
import de.interact.domain.shared.TestId
import de.interact.domain.shared.UnitTestId
import de.interact.domain.testtwin.api.event.InteractionTestAddedEvent
import de.interact.domain.testtwin.api.event.UnitTestAddedEvent
import de.interact.domain.testtwin.spi.InteractionTestAddedEventListener
import de.interact.domain.testtwin.spi.UnitTestAddedEventListener
import java.util.LinkedList

class ValidationPlansManager(
    private val tests: Tests,
    private val validationPlans: ValidationPlans,
    private val unitTestBasedInteractionExpectations: UnitTestBasedInteractionExpectations,
    private val interfaces: Interfaces
): UnitTestAddedEventListener, InteractionTestAddedEventListener, UnitTestBasedInteractionExpectationAddedEventListener {
    override fun onUnitTestCaseAdded(event: UnitTestAddedEvent) {
        onTestAdded(event.test)
    }

    override fun onInteractionTestAdded(event: InteractionTestAddedEvent) {
        onTestAdded(event.test)
    }

    private fun onTestAdded(test: EntityReference<TestId>) {
        val test = tests.find(test.id)!!
        val dependantValidationPlans = validationPlans.waitingFor(test)
        val updatedValidationPlans = dependantValidationPlans.map {
            val updatedPlan = it.handle(test)
            validationPlans.save(updatedPlan)
        }
        updatedValidationPlans.forEach {
            // TODO("tryToProgressValidationPlan(it)")
        }
    }

    override fun onUnitTestBasedInteractionExpectationAdded(
        event: InteractionExpectationAddedEvent.UnitTestBasedInteractionExpectationAddedEvent
    ) {
        val interactionExpectation = unitTestBasedInteractionExpectations.find(event.interactionExpectationId)!!
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
                    setOf((derivedFromTest.triggeredMessages.first() as StimulusMessage).receivedBy),
                    setOf(
                        derivedFromTest.triggeredMessages
                            .filterIsInstance<ComponentResponseMessage>()
                            .first{interactionExpectation.expectFrom.id == it.id}.sentBy
                    )
                )
            )
        )

        while (interactionGraphsToExpand.isNotEmpty()) {
            val interactionGraphToExpand = interactionGraphsToExpand.poll()
            val expandedInteractionGraphs = expandInteractionGraph(interactionGraphToExpand)
            expandedInteractionGraphs.forEach { interactionGraph ->
                if (interactionGraph.leadsTo(interactionExpectation.expectTo.map { it.id }.toSet())) {
                    interactionGraphs.add(interactionGraph)
                } else if (interactionGraph != interactionGraphToExpand) {
                    interactionGraphsToExpand.offer(interactionGraph)
                }
            }
        }
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
                        updatedGraph
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
                val nextSegments = unitTests.map { unitTest ->
                    //TODO filter message if next message in flow of test
                    Interaction.Pending(
                        EntityReference(UnitTestId(unitTest.id.value), unitTest.version),
                        TestCase.IncompleteTestCase(
                            unitTest.derivedFrom,
                            setOf(
                                //TODO multiple replacements
                                Replacement(
                                    MessageToReplaceIdentifier(
                                        unitTest.triggeredMessages.filterIsInstance<Message.ReceivedMessage>()
                                            .first { it.receivedBy.id == nextInterface.id }.toEntityReference(),
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
                            .filter { it.dependsOn.map { it.id }.contains(
                                unitTest.triggeredMessages.filterIsInstance<Message.ReceivedMessage>()
                                    .first { it.receivedBy.id == nextInterface.id }.id
                            ) }.map { it.sentBy }.toSet()
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
}