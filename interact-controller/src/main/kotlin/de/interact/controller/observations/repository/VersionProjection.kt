package de.interact.controller.observations.repository

import de.interact.controller.persistence.domain.*
import de.interact.domain.shared.*
import de.interact.domain.testtwin.Version
import de.interact.domain.testtwin.VersionIdentifier
import de.interact.domain.testtwin.abstracttest.AbstractTestCase
import de.interact.domain.testtwin.abstracttest.AbstractTestCaseIdentifier
import de.interact.domain.testtwin.abstracttest.concretetest.ConcreteTestCaseIdentifier
import de.interact.domain.testtwin.abstracttest.concretetest.InteractionTest
import de.interact.domain.testtwin.abstracttest.concretetest.TestParameter
import de.interact.domain.testtwin.abstracttest.concretetest.UnitTest
import de.interact.domain.testtwin.abstracttest.concretetest.message.*
import de.interact.domain.testtwin.abstracttest.concretetest.message.Message
import de.interact.domain.testtwin.componentinterface.IncomingInterface
import de.interact.domain.testtwin.componentinterface.OutgoingInterface
import java.util.*

interface VersionProjection {
    val id: UUID
    val identifier: String
    val versionOf: VersionOf
    val testedBy: Set<TestedBy>
    val listeningTo: Set<ListeningTo>
    val sendingTo: Set<SendingTo>
    val version: Long?

    interface VersionOf {
        val id: UUID
        val version: Long?
    }

    interface TestedBy {
        val id: UUID
        val identifier: String
        val templateFor: Set<TemplateFor>
        val version: Long?

        interface TemplateFor {
            val id: UUID
            val identifier: String
            val labels: Set<String>
            val triggeredMessages: Set<TriggeredMessage>
            val parameters: List<String>
            val status: String
            val version: Long?

            interface TriggeredMessage {
                val id: UUID
                val order: Int
                val payload: String
                val labels: Set<String>
                val receivedBy: ListeningTo?
                val sentBy: SendingTo?
                val dependsOn: Set<ReceivedMessageReferenceProjection>
                val reactionTo: ComponentResponseReferenceProjection?
                val version: Long?
            }
        }
    }

    interface ListeningTo: IncomingInterfaceReferenceProjection {
        val protocol: String
        val protocolData: Map<String, String>
    }

    interface SendingTo: OutgoingInterfaceReferenceProjection {
        val protocol: String
        val protocolData: Map<String, String>
    }
}

fun VersionProjection.toVersion(): Version {
    val atLeastOneMessageReceivedOn = mutableSetOf<IncomingInterface>()
    val atLeastOneMessageSendTo = mutableSetOf<OutgoingInterface>()

    val mapMessages: (concreteTestCase: VersionProjection.TestedBy.TemplateFor) -> SortedSet<Message> =
        { concreteTest ->

            val convertedMessages = mutableListOf<Message>()

            concreteTest.triggeredMessages.forEach { message ->
                if (message.labels.contains(STIMULUS_NODE_LABEL)) {
                    StimulusMessage(
                        StimulusMessageId(message.id),
                        MessageValue(message.payload),
                        message.receivedBy!!.toEntityReference(),
                        message.version
                    ).also {
                        convertedMessages += it
                    }
                } else if (message.labels.contains(COMPONENT_RESPONSE_NODE_LABEL)) {
                    ComponentResponseMessage(
                        ComponentResponseMessageId(message.id),
                        MessageValue(message.payload),
                        convertedMessages.size,
                        message.sentBy!!.toEntityReference(),
                        message.dependsOn.map {
                            it.toEntityReference()
                        },
                        message.version
                    ).also {
                        convertedMessages += it
                    }
                } else if (message.labels.contains(ENVIRONMENT_RESPONSE_NODE_LABEL)) {
                    EnvironmentResponseMessage(
                        EnvironmentResponseMessageId(message.id),
                        MessageValue(message.payload),
                        convertedMessages.size,
                        message.receivedBy!!.toEntityReference(),
                        message.reactionTo!!.toEntityReference(),
                        message.version
                    ).also {
                        convertedMessages += it
                    }
                } else {
                    throw IllegalStateException("Unknown message type")
                }
            }
            convertedMessages.toSortedSet()
        }

    return Version(
        EntityReference(ComponentId(versionOf.id), versionOf.version),
        VersionIdentifier(identifier),
        testedBy.map { abstractTest ->
            AbstractTestCase(
                AbstractTestId(abstractTest.id),
                AbstractTestCaseIdentifier(abstractTest.identifier),
                abstractTest.templateFor.map { concreteTest ->
                    if (concreteTest.labels.contains(UNIT_TEST_NODE_LABEL)) {
                        UnitTest(
                            UnitTestId(concreteTest.id),
                            ConcreteTestCaseIdentifier(concreteTest.identifier),
                            concreteTest.parameters.map { TestParameter(it) },
                            mapMessages(concreteTest),
                            TestState.fromString(concreteTest.status),
                            concreteTest.version
                        )
                    } else {
                        InteractionTest(
                            InteractionTestId(concreteTest.id),
                            ConcreteTestCaseIdentifier(concreteTest.identifier),
                            concreteTest.parameters.map { TestParameter(it) },
                            mapMessages(concreteTest),
                            TestState.fromString(concreteTest.status),
                            concreteTest.version
                        )
                    }
                }.toSet(),
                abstractTest.version
            )
        }.toSet(),
        listeningTo.map {
            val receivedBy =
                IncomingInterface(IncomingInterfaceId(it.id), Protocol(it.protocol), ProtocolData(it.protocolData), it.version)
            atLeastOneMessageReceivedOn.firstOrNull { it == receivedBy }
                ?: receivedBy.also { atLeastOneMessageReceivedOn.add(it) }
        }.toSet(),
        sendingTo.map {
            val sentBy =
                OutgoingInterface(OutgoingInterfaceId(it.id), Protocol(it.protocol), ProtocolData(it.protocolData), it.version)
            atLeastOneMessageSendTo.firstOrNull { it == sentBy } ?: sentBy.also { atLeastOneMessageSendTo.add(it) }
        }.toSet(),
        VersionId(id),
        version
    )
}

