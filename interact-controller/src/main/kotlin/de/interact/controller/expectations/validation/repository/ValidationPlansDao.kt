package de.interact.controller.expectations.validation.repository

import arrow.core.Either.Companion.catch
import arrow.core.getOrElse
import de.interact.controller.persistence.domain.*
import de.interact.domain.expectations.TestParameter
import de.interact.domain.expectations.validation.events.ValidationPlanUpdatedEvent
import de.interact.domain.expectations.validation.plan.*
import de.interact.domain.expectations.validation.spi.ValidationPlans
import de.interact.domain.expectations.validation.test.Test
import de.interact.domain.shared.*
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.neo4j.core.Neo4jTemplate
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import java.util.*

@Repository
interface ValidationPlansRepository :
    org.springframework.data.repository.Repository<InteractionExpectationValidationPlanEntity, UUID> {
    @Query(
        "MATCH p = (vp:$INTERACTION_EXPECTATION_VALIDATION_PLAN_NODE_LABEL)-[:$INTERACTION_GRAPH_RELATIONSHIP_LABEL]->" +
                "(:$INTERACTION_GRAPH_NODE_LABEL)-[:$CONSISTS_OF_RELATIONSHIP_LABEL]->" +
                "(:$INTERACTION_NODE_LABEL)-[:$VALIDATED_BY_RELATIONSHIP_LABEL]->" +
                "(:$EXECUTABLE_TEST_CASE_NODE_LABEL{parameters:\$parameters})-[:$DERIVED_FROM_ABSTRACT_TEST_CASE_RELATIONSHIP_LABEL]->(:$ABSTRACT_TEST_CASE_NODE_LABEL{id:\$derivedFrom}) " +
                "RETURN vp.id"
    )
    fun findValidationPlanWaitingForTest(
        derivedFrom: UUID,
        parameters: List<String>
    ): List<UUID>
    fun findValidationPlanById(id: UUID): ValidationPlanProjection?
    fun findValidationPlanByCandidateForId(value: UUID): Set<ValidationPlanProjection>
}

@Service
class ValidationPlansDao(
    private val neo4jTemplate: Neo4jTemplate,
    private val repository: ValidationPlansRepository,
    private val applicationEventPublisher: ApplicationEventPublisher
) : ValidationPlans {
    override fun find(id: ValidationPlanId): ValidationPlan? {
        return repository.findValidationPlanById(id.value)?.toDomain()
    }

    override fun waitingFor(test: Test): Set<ValidationPlan.PendingValidationPlan> {
        return repository.findValidationPlanWaitingForTest(
            test.derivedFrom.id.value,
            test.parameters.map { it.toString() }
        ).map { repository.findValidationPlanById(it)!!.toDomain() as ValidationPlan.PendingValidationPlan }.toSet()
    }

    override fun save(validationPlan: ValidationPlan): ValidationPlan {
        return neo4jTemplate.saveAs(validationPlan.toEntity(), ValidationPlanProjection::class.java).toDomain().also {
            applicationEventPublisher.publishEvent(ValidationPlanUpdatedEvent(it.id))
        }
    }

    override fun save(validationPlans: Collection<ValidationPlan>): Collection<ValidationPlan> {
        return neo4jTemplate.saveAllAs(validationPlans.map { it.toEntity() }, ValidationPlanProjection::class.java).map{
            it.toDomain().also {
                applicationEventPublisher.publishEvent(ValidationPlanUpdatedEvent(it.id))
            }
        }
    }

    override fun findByInteractionExpectationId(id: UnitTestBasedInteractionExpectationId): List<ValidationPlan> {
        return repository.findValidationPlanByCandidateForId(id.value).map { it.toDomain() }
    }
}

interface ValidationPlanProjection: ValidationPlanReferenceProjection {
    val candidateFor: InteractionExpectationReferenceProjection
    val interactionGraph: InteractionGraphProjection
    val status: String

    interface InteractionGraphProjection: InteractionGraphReferenceProjection {
        val interactions: Set<InteractionProjection>

        interface InteractionProjection: InteractionReferenceProjection {
            val previous: Set<EntityReferenceProjection>
            val derivedFrom: UnitTestReferenceProjection
            val from: Set<ReceivedMessageProjection>
            val to: Set<SentMessageProjection>
            val order: Int
            val testCase: TestCaseProjection

            interface TestCaseProjection: TestCaseReferenceProjection {
                val labels: Set<String>
                val derivedFrom: AbstractTestCaseReferenceProjection
                val parameters: List<String>
                val actualTest: ConcreteTestCaseReferenceProjection
                val replacements: Set<ReplacementProjection>

                interface ReplacementProjection: ReplacementReferenceProjection{
                    val messageToReplace: ReceivedMessageProjection
                    val replaceWithMessage: SentMessageProjection
                }
            }

            interface ReceivedMessageProjection : ReceivedMessageReferenceProjection {
                val receivedBy: IncomingInterfaceReferenceProjection
            }

            interface SentMessageProjection : SentMessageReferenceProjection {
                val sentBy: OutgoingInterfaceReferenceProjection
            }
        }
    }
}

private fun ValidationPlan.toEntity(): InteractionExpectationValidationPlanEntity {

    val interactionsToMap = interactionGraph.source?.let { mutableSetOf(it) } ?: mutableSetOf()
    val mappedInteractions: MutableMap<InteractionId,InteractionEntity> = mutableMapOf()
    while (interactionsToMap.isNotEmpty()) {
        val current = interactionsToMap.first()
        interactionsToMap.remove(current)

        var map: (Interaction) -> InteractionEntity = { interaction ->
            val creator = when(interaction) {
                is Interaction.Pending -> ::pendingInteractionEntity
                is Interaction.Executable -> ::executableInteractionEntity
                is Interaction.Finished.Validated -> ::validatedInteractionEntity
                is Interaction.Finished.Failed -> ::failedInteractionEntity
            }

            creator(
                interaction.id,
                catch{Collections.max(interactionGraph.reverseAdjacencyMap[interaction]!!.map { mappedInteractions[it.id]!!.order+1 })}.getOrElse { 0 },
                interaction.version,
                interactionGraph.reverseAdjacencyMap[interaction]!!.map { mappedInteractions[it.id]!! }.toSet(),
                interaction.derivedFrom.toEntity(),
                interaction.from.map { it.second.toEntity().apply { receivedBy = it.first.toEntity() } }.toSet(),
                interaction.to.map { it.second.toEntity().apply { sentBy = it.first.toEntity() } }.toSet(),
                when (interaction.testCase) {
                    is TestCase.IncompleteTestCase -> incompleteTestCaseEntity(
                        interaction.testCase.id,
                        interaction.testCase.version,
                        interaction.testCase.replacements.map {
                            replacementEntity(
                                it.id,
                                it.version,
                                it.messageToReplace.messageInOriginalUnitTest.toEntity().also { m ->
                                    m.receivedBy = it.messageToReplace.interfaceReference.toEntity()
                                },
                                it.replacement.messageInOriginalUnitTest.toEntity()
                            ).apply {
                                messageToReplace.receivedBy = it.messageToReplace.interfaceReference.toEntity()
                                replaceWithMessage.sentBy = it.replacement.interfaceToCopyFrom.toEntity()
                            }
                        }.toSet(),
                        (interaction.testCase as TestCase.IncompleteTestCase).derivedFrom.toEntity()
                    )

                    is TestCase.ExecutableTestCase -> executableTestCaseEntity(
                        interaction.testCase.id,
                        interaction.testCase.version,
                        interaction.testCase.replacements.map {
                            replacementEntity(
                                it.id,
                                it.version,
                                it.messageToReplace.messageInOriginalUnitTest.toEntity().also { m ->
                                    m.receivedBy = it.messageToReplace.interfaceReference.toEntity()
                                },
                                it.replacement.messageInOriginalUnitTest.toEntity()
                            ).apply {
                                messageToReplace.receivedBy = it.messageToReplace.interfaceReference.toEntity()
                                replaceWithMessage.sentBy = it.replacement.interfaceToCopyFrom.toEntity()
                            }
                        }.toSet(),
                        (interaction.testCase as TestCase.ExecutableTestCase).derivedFrom.toEntity(),
                        (interaction.testCase as TestCase.ExecutableTestCase).parameters.map { it.toString() }
                    )

                    is TestCase.CompleteTestCase.Succeeded -> succeededTestCaseEntity(
                        interaction.testCase.id,
                        interaction.testCase.version,
                        interaction.testCase.replacements.map {
                            replacementEntity(
                                it.id,
                                it.version,
                                it.messageToReplace.messageInOriginalUnitTest.toEntity().also { m ->
                                    m.receivedBy = it.messageToReplace.interfaceReference.toEntity()
                                },
                                it.replacement.messageInOriginalUnitTest.toEntity()
                            ).apply {
                                messageToReplace.receivedBy = it.messageToReplace.interfaceReference.toEntity()
                                replaceWithMessage.sentBy = it.replacement.interfaceToCopyFrom.toEntity()
                            }
                        }.toSet(),
                        (interaction.testCase as TestCase.CompleteTestCase.Succeeded).derivedFrom.toEntity(),
                        (interaction.testCase as TestCase.CompleteTestCase.Succeeded).parameters.map { it.toString() },
                        (interaction.testCase as TestCase.CompleteTestCase.Succeeded).actualTest.toEntity()
                    )

                    is TestCase.CompleteTestCase.Failed -> failedTestCaseEntity(
                        interaction.testCase.id,
                        interaction.testCase.version,
                        interaction.testCase.replacements.map {
                            replacementEntity(
                                it.id,
                                it.version,
                                it.messageToReplace.messageInOriginalUnitTest.toEntity().also { m ->
                                    m.receivedBy = it.messageToReplace.interfaceReference.toEntity()
                                },
                                it.replacement.messageInOriginalUnitTest.toEntity()
                            ).apply {
                                messageToReplace.receivedBy = it.messageToReplace.interfaceReference.toEntity()
                                replaceWithMessage.sentBy = it.replacement.interfaceToCopyFrom.toEntity()
                            }
                        }.toSet(),
                        (interaction.testCase as TestCase.CompleteTestCase.Failed).derivedFrom.toEntity(),
                        (interaction.testCase as TestCase.CompleteTestCase.Failed).parameters.map { it.toString() },
                        (interaction.testCase as TestCase.CompleteTestCase.Failed).actualTest.toEntity()
                    )
                }
            )
        }

        mappedInteractions[current.id] = map(current)
        interactionGraph.adjacencyMap[current]?.forEach {
            if(mappedInteractions.keys.containsAll(interactionGraph.reverseAdjacencyMap[it]!!.map { it.id })) {
                interactionsToMap.add(it)
            }
        }
    }

    return interactionExpectationValidationPlanEntity(
        id,
        version,
        candidateFor.toEntity(),
        interactionGraphEntity(
            interactionGraph.id,
            interactionGraph.version,
            mappedInteractions.values.toSortedSet()
        ),
        when (this) {
            is ValidationPlan.PendingValidationPlan -> "PENDING"
            is ValidationPlan.ValidatedValidationPlan -> "VALIDATED"
            is ValidationPlan.FailedValidationPlan -> "FAILED"
        }
    )
}

private fun ValidationPlanProjection.toDomain(): ValidationPlan {

    var mappedGraph = InteractionGraph(version = interactionGraph.version)
    val interactions = interactionGraph.interactions.map { interaction ->
        when {
            interaction.labels.contains(PENDING_INTERACTION_NODE_LABEL) -> Interaction.Pending(
                interaction.derivedFrom.toEntityReference(),
                when {
                    interaction.testCase.labels.contains(INCOMPLETE_TEST_CASE_NODE_LABEL) -> TestCase.IncompleteTestCase(
                        interaction.testCase.derivedFrom.toEntityReference(),
                        interaction.testCase.replacements.map {
                            Replacement(
                                MessageToReplaceIdentifier(
                                    it.messageToReplace.toEntityReference(),
                                    it.messageToReplace.receivedBy.toEntityReference()
                                ),
                                ReplacementIdentifier(
                                    it.replaceWithMessage.toEntityReference(),
                                    it.replaceWithMessage.sentBy.toEntityReference()
                                ),
                                ReplacementId(it.id),
                                it.version
                            )
                        }.toSet(),
                        TestDefinitionId(interaction.testCase.id),
                        interaction.testCase.version
                    )

                    else -> throw IllegalArgumentException("Unknown test case type: ${interaction.testCase.labels}")
                },
                interaction.from.map {
                    it.receivedBy.toEntityReference() to it.toEntityReference()
                }.toSet(),
                interaction.to.map {
                    it.sentBy.toEntityReference() to it.toEntityReference()
                }.toSet(),
                InteractionId(interaction.id),
                interaction.version
            )

            interaction.labels.contains(EXECUTABLE_INTERACTION_NODE_LABEL) -> Interaction.Executable(
                interaction.derivedFrom.toEntityReference(),
                when {
                    interaction.testCase.labels.contains(EXECUTABLE_TEST_CASE_NODE_LABEL) -> TestCase.ExecutableTestCase(
                        interaction.testCase.derivedFrom.toEntityReference(),
                        interaction.testCase.replacements.map {
                            Replacement(
                                MessageToReplaceIdentifier(
                                    it.messageToReplace.toEntityReference(),
                                    it.messageToReplace.receivedBy.toEntityReference()
                                ),
                                ReplacementIdentifier(
                                    it.replaceWithMessage.toEntityReference(),
                                    it.replaceWithMessage.sentBy.toEntityReference()
                                ),
                                ReplacementId(it.id),
                                it.version
                            )
                        }.toSet(),
                        interaction.testCase.parameters.map { TestParameter(it) },
                        TestDefinitionId(interaction.testCase.id),
                        interaction.testCase.version
                    )

                    else -> throw IllegalArgumentException("Unknown test case type: ${interaction.testCase.labels}")
                },
                interaction.from.map {
                    it.receivedBy.toEntityReference() to it.toEntityReference()
                }.toSet(),
                interaction.to.map {
                    it.sentBy.toEntityReference() to it.toEntityReference()
                }.toSet(),
                InteractionId(interaction.id),
                interaction.version
            )

            interaction.labels.contains(VALIDATED_INTERACTION_NODE_LABEL) -> Interaction.Finished.Validated(
                interaction.derivedFrom.toEntityReference(),
                when {
                    interaction.testCase.labels.contains(VALIDATED_TEST_CASE_NODE_LABEL) -> TestCase.CompleteTestCase.Succeeded(
                        interaction.testCase.derivedFrom.toEntityReference(),
                        interaction.testCase.replacements.map {
                            Replacement(
                                MessageToReplaceIdentifier(
                                    it.messageToReplace.toEntityReference(),
                                    it.messageToReplace.receivedBy.toEntityReference()
                                ),
                                ReplacementIdentifier(
                                    it.replaceWithMessage.toEntityReference(),
                                    it.replaceWithMessage.sentBy.toEntityReference()
                                ),
                                ReplacementId(it.id),
                                it.version
                            )
                        }.toSet(),
                        interaction.testCase.parameters.map { TestParameter(it) },
                        interaction.testCase.actualTest.toEntityReference(),
                        TestDefinitionId(interaction.testCase.id),
                        interaction.testCase.version
                    )

                    else -> throw IllegalArgumentException("Unknown test case type: ${interaction.testCase.labels}")
                },
                interaction.from.map {
                    it.receivedBy.toEntityReference() to it.toEntityReference()
                }.toSet(),
                interaction.to.map {
                    it.sentBy.toEntityReference() to it.toEntityReference()
                }.toSet(),
                InteractionId(interaction.id),
                interaction.version
            )

            interaction.labels.contains(FAILED_INTERACTION_NODE_LABEL) -> Interaction.Finished.Failed(
                interaction.derivedFrom.toEntityReference(),
                when {
                    interaction.testCase.labels.contains(FAILED_TEST_CASE_NODE_LABEL) -> TestCase.CompleteTestCase.Failed(
                        interaction.testCase.derivedFrom.toEntityReference(),
                        interaction.testCase.replacements.map {
                            Replacement(
                                MessageToReplaceIdentifier(
                                    it.messageToReplace.toEntityReference(),
                                    it.messageToReplace.receivedBy.toEntityReference()
                                ),
                                ReplacementIdentifier(
                                    it.replaceWithMessage.toEntityReference(),
                                    it.replaceWithMessage.sentBy.toEntityReference()
                                ),
                                ReplacementId(it.id),
                                it.version
                            )
                        }.toSet(),
                        interaction.testCase.parameters.map { TestParameter(it) },
                        interaction.testCase.actualTest.toEntityReference(),
                        TestDefinitionId(interaction.testCase.id),
                        interaction.testCase.version
                    )

                    else -> throw IllegalArgumentException("Unknown test case type: ${interaction.testCase.labels}")
                },
                interaction.from.map {
                    it.receivedBy.toEntityReference() to it.toEntityReference()
                }.toSet(),
                interaction.to.map {
                    it.sentBy.toEntityReference() to it.toEntityReference()
                }.toSet(),
                InteractionId(interaction.id),
                interaction.version
            )

            else -> throw IllegalArgumentException("Unknown interaction type: ${interaction.labels}")
        }
    }

    interactionGraph.interactions.forEach { interaction ->
        val previous = interaction.previous.map { previous ->
            interactions.find { it.id.value == previous.id }!!
        }.toSet()
        val mappedInteraction = interactions.first { it.id.value == interaction.id }
        mappedGraph = mappedGraph.addInteraction(mappedInteraction, previous)
    }

    return when {
        status == "PENDING" -> ValidationPlan.PendingValidationPlan(
            candidateFor.toEntityReference(),
            mappedGraph,
            ValidationPlanId(id),
            version
        )
        status == "VALIDATED" -> ValidationPlan.ValidatedValidationPlan(
            candidateFor.toEntityReference(),
            mappedGraph,
            ValidationPlanId(id),
            version
        )
        status == "FAILED" -> ValidationPlan.FailedValidationPlan(
            candidateFor.toEntityReference(),
            mappedGraph,
            ValidationPlanId(id),
            version
        )

        else -> throw IllegalArgumentException("Unknown status: $status")
    }
}