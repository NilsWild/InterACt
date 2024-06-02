package de.interact.controller.expectations.validation.repository

import de.interact.controller.persistence.domain.*
import de.interact.domain.expectations.TestParameter
import de.interact.domain.expectations.shared.MessageValue
import de.interact.domain.expectations.validation.`interface`.Interface
import de.interact.domain.expectations.validation.spi.Tests
import de.interact.domain.expectations.validation.test.*
import de.interact.domain.shared.*
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import java.util.*

@Repository
interface TestsRepository: org.springframework.data.repository.Repository<ConcreteTestCaseEntity, UUID> {
    fun findTestById(id: UUID): TestProjection?
    @Query("MATCH (t:$UNIT_TEST_NODE_LABEL)-[:$TRIGGERED_MESSAGES_RELATIONSHIP_LABEL]->(:$RECEIVED_MESSAGE_NODE_LABEL)" +
            "-[:$RECEIVED_BY_RELATIONSHIP_LABEL]->(i:$INCOMING_INTERFACE_NODE_LABEL{id:\$receivedByInterfaceId}) " +
            "MATCH p = (t)-[:$TRIGGERED_MESSAGES_RELATIONSHIP_LABEL]->()" +
            "-[:$DEPENDS_ON_RELATIONSHIP_LABEL|$REACTION_TO_RELATIONSHIP_LABEL" +
            "|$RECEIVED_BY_RELATIONSHIP_LABEL|$SENT_BY_RELATIONSHIP_LABEL]->() " +
            "MATCH p2 = (t)<-[:$TEMPLATE_FOR_RELATIONSHIP_LABEL]-() " +
            "RETURN t, collect(nodes(p))+collect(nodes(p2)), collect(relationships(p))+collect(relationships(p2))")
    fun findUnitTestThatReceivedMessageByInterface(
        receivedByInterfaceId: UUID
    ): Set<TestProjection>
}

@Service
class TestsDao(
    private val repository: TestsRepository
): Tests {
    override fun find(testId: TestId): Test? {
        return repository.findTestById(testId.value)?.toDomain()
    }

    override fun findUnitTestsReceivingBy(nextInterface: Interface.IncomingInterface): Set<Test> {
        return repository.findUnitTestThatReceivedMessageByInterface(nextInterface.id.value).map { it.toDomain() }.toSet()
    }
}

interface TestProjection: ConcreteTestCaseReferenceProjection {
    val template: AbstractTestCaseReferenceProjection
    val parameters: List<String>
    val triggeredMessages: Set<ComplexMessageReference>

    interface ComplexMessageReference: MessageReferenceProjection {
        val order: Int
        val payload: String
        val sentBy: OutgoingInterfaceReferenceProjection
        val receivedBy: IncomingInterfaceReferenceProjection
        val reactionTo: ComponentResponseReferenceProjection
        val dependsOn: Set<ReceivedMessageReferenceProjection>
    }
}

fun TestProjection.toDomain(): Test {
    val convertedMessages = emptySet<Message>().toMutableSet()

    triggeredMessages.forEach { messageToConvert ->
        when {
            messageToConvert.labels.contains(COMPONENT_RESPONSE_NODE_LABEL) -> convertedMessages += ComponentResponseMessage(
                ComponentResponseMessageId(messageToConvert.id),
                messageToConvert.version!!,
                MessageValue(messageToConvert.payload),
                messageToConvert.order,
                messageToConvert.sentBy.toEntityReference(),
                messageToConvert.dependsOn.map { depends -> convertedMessages.first { it.id.value == depends.id } as Message.ReceivedMessage }
            )
            messageToConvert.labels.contains(ENVIRONMENT_RESPONSE_NODE_LABEL) -> convertedMessages += EnvironmentResponseMessage(
                EnvironmentResponseMessageId(messageToConvert.id),
                messageToConvert.version!!,
                MessageValue(messageToConvert.payload),
                messageToConvert.order,
                messageToConvert.receivedBy.toEntityReference(),
                convertedMessages.first { it.id.value == messageToConvert.reactionTo.id } as ComponentResponseMessage
            )
            messageToConvert.labels.contains(STIMULUS_NODE_LABEL) -> convertedMessages += StimulusMessage(
                StimulusMessageId(messageToConvert.id),
                messageToConvert.version!!,
                MessageValue(messageToConvert.payload),
                messageToConvert.receivedBy.toEntityReference()
            )
            else -> throw IllegalArgumentException("Unknown message type")
        }
    }

    return Test(
        when{
            labels.contains(UNIT_TEST_NODE_LABEL) -> UnitTestId(id)
            labels.contains(INTERACTION_TEST_NODE_LABEL) -> InteractionTestId(id)
            else -> throw IllegalArgumentException("Unknown test type")
        },
        version!!,
        template.toEntityReference(),
        parameters.map { TestParameter(it) },
        convertedMessages.toSortedSet()
    )
}