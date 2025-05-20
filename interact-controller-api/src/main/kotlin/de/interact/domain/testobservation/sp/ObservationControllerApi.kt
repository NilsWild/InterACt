package de.interact.domain.testobservation.sp

import com.fasterxml.uuid.Generators
import de.interact.domain.serialization.SerializationConstants
import de.interact.domain.shared.*
import de.interact.domain.testobservation.ObservationToTwinMapper
import de.interact.domain.testobservation.model.InteractionTestCase
import de.interact.domain.testobservation.model.TestObservation
import de.interact.domain.testobservation.model.UnitTestCase
import de.interact.domain.testobservation.spi.ObservationPublisher
import de.interact.domain.testtwin.Component
import de.interact.domain.testtwin.ComponentIdentifier
import de.interact.domain.testtwin.Version
import de.interact.domain.testtwin.VersionIdentifier
import de.interact.domain.testtwin.abstracttest.AbstractTestCase
import de.interact.domain.testtwin.abstracttest.concretetest.InteractionTest
import de.interact.domain.testtwin.abstracttest.concretetest.TestParameter
import de.interact.domain.testtwin.abstracttest.concretetest.UnitTest
import de.interact.domain.testtwin.abstracttest.concretetest.message.*
import de.interact.domain.testtwin.abstracttest.concretetest.message.ComponentResponseMessage
import de.interact.domain.testtwin.abstracttest.concretetest.message.Message
import de.interact.domain.testtwin.abstracttest.concretetest.testCaseIdentifier
import de.interact.domain.testtwin.api.dto.PartialComponentVersionModel
import de.interact.domain.testtwin.componentinterface.IncomingInterface
import de.interact.domain.testtwin.componentinterface.OutgoingInterface
import de.interact.utils.Logging
import de.interact.utils.logger
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.client.WebClient
import java.util.*
import de.interact.domain.testobservation.model.ComponentResponseMessage as ObservedComponentResponseMessage
import de.interact.domain.testobservation.model.EnvironmentResponseMessage as ObservedEnvironmentResponseMessage
import de.interact.domain.testobservation.model.Message as ObservedMessage
import de.interact.domain.testobservation.model.StimulusMessage as ObservedStimulusMessage

class ObservationControllerApi(private val url: String, vertx: Vertx) :
    ObservationPublisher,
    Logging {

    private val client: WebClient
    private val log = logger()

    init {
        client = WebClient.create(vertx)
    }

    override fun publish(observation: TestObservation): Boolean {
        val partialModel = mapObservationToPartialModel(observation)
        val body = SerializationConstants.mapper.writeValueAsBytes(partialModel)
        log.info("Storing observations")
        client.postAbs("$url/api/observations")
            .putHeader("Content-Type", "application/json").timeout(30000)
            .sendBuffer(Buffer.buffer(body))
            .onSuccess {
                if (it.statusCode() != 200) {
                    log.error("Could not store observations. Error: ${it.bodyAsString()}")
                } else {
                    log.info("Stored observations")
                }
            }.onFailure {
                log.error("Could not store observations", it)
            }.toCompletionStage().toCompletableFuture().join()
        return true
    }

    private fun mapObservationToPartialModel(observation: TestObservation): PartialComponentVersionModel {
        val observedComponent = observation.observedComponents.first()
        val componentId = ObservationToTwinMapper.componentId(observedComponent)
        val versionId = ObservationToTwinMapper.versionId(componentId, observedComponent.version)
        val testCases = observedComponent.testedBy.map { abstractTestCase ->
            val abstractTestCaseId = ObservationToTwinMapper.abstractTestCaseId(versionId, abstractTestCase)
            AbstractTestCase(
                abstractTestCaseId,
                ObservationToTwinMapper.abstractTestCaseIdentifier(abstractTestCase),
                abstractTestCase.templateFor.map { testCase ->
                    when (testCase) {
                        is UnitTestCase -> UnitTest(
                            UnitTestId(
                                Generators.nameBasedGenerator()
                                    .generate("$abstractTestCaseId:UnitTest:${testCase.testCaseIdentifier()}")
                            ),
                            testCase.testCaseIdentifier(),
                            testCase.parameters.map { TestParameter(it.value) },
                            toMessages(versionId, testCase.observedBehavior.messageSequence),
                            testCase.status
                        )

                        is InteractionTestCase -> InteractionTest(
                            InteractionTestId(
                                Generators.nameBasedGenerator()
                                    .generate("$abstractTestCaseId:InteractionTest:${testCase.testCaseIdentifier()}")
                            ),
                            testCase.testCaseIdentifier(),
                            testCase.parameters.map { TestParameter(it.value) },
                            toMessages(versionId, testCase.observedBehavior.messageSequence),
                            testCase.status
                        )
                    }
                }.toSet()
            )
        }.toSet()
        val component = Component(
            componentId,
            ComponentIdentifier(observedComponent.name.value)
        )
        return PartialComponentVersionModel(
            component,
            Version(
                EntityReference(component),
                VersionIdentifier(observedComponent.version.value),
                testCases,
                observedComponent.testedBy.asSequence().flatMap { it.templateFor }.map { it.observedBehavior }.flatMap { it.messageSequence }
                    .filterIsInstance<de.interact.domain.testobservation.model.Message.ReceivedMessage>()
                    .map { message ->
                        IncomingInterface(
                            IncomingInterfaceId(
                                Generators.nameBasedGenerator()
                                    .generate("$versionId:IncomingInterface:${message.receivedBy.protocol}:${message.receivedBy.protocolData}")
                            ),
                            Protocol(message.receivedBy.protocol.value),
                            ProtocolData(message.receivedBy.protocolData.data)
                        )
                    }.toSet(),
                observedComponent.testedBy.asSequence().flatMap { it.templateFor }.map { it.observedBehavior }.flatMap { it.messageSequence }
                    .filterIsInstance<de.interact.domain.testobservation.model.Message.SentMessage>()
                    .map { message ->
                        OutgoingInterface(
                            OutgoingInterfaceId(
                                Generators.nameBasedGenerator()
                                    .generate("$versionId:OutgoingInterface:${message.sentBy.protocol}:${message.sentBy.protocolData}")
                            ),
                            Protocol(message.sentBy.protocol.value),
                            ProtocolData(message.sentBy.protocolData.data)
                        )
                    }.toSet(),
                versionId
            )
        )
    }

    private fun toMessages(versionId: VersionId, messages: SortedSet<ObservedMessage>): SortedSet<Message> {

        val convertedMessages = mutableListOf<Message>()

        messages.forEach { message: ObservedMessage ->
            when (message) {
                is ObservedStimulusMessage -> {
                    val receivedBy = IncomingInterface(
                        IncomingInterfaceId(
                            Generators.nameBasedGenerator()
                                .generate("$versionId:IncomingInterface:${message.receivedBy.protocol}:${message.receivedBy.protocolData}")
                        ),
                        Protocol(message.receivedBy.protocol.value),
                        ProtocolData(message.receivedBy.protocolData.data)
                    )
                    StimulusMessage(
                        StimulusMessageId(UUID.randomUUID()),
                        MessageValue(message.value.value),
                        EntityReference(receivedBy.id, null)
                    ).also {
                        convertedMessages += it
                    }
                }

                is ObservedComponentResponseMessage -> {
                    val sentBy = OutgoingInterface(
                        OutgoingInterfaceId(
                            Generators.nameBasedGenerator()
                                .generate("$versionId:OutgoingInterface:${message.sentBy.protocol}:${message.sentBy.protocolData}")
                        ),
                        Protocol(message.sentBy.protocol.value),
                        ProtocolData(message.sentBy.protocolData.data)
                    )
                    ComponentResponseMessage(
                        ComponentResponseMessageId(UUID.randomUUID()),
                        MessageValue(message.value.value),
                        convertedMessages.size,
                        EntityReference(sentBy.id, null),
                        message.dependsOn.map {
                            val m = convertedMessages[it.order] as Message.ReceivedMessage
                            EntityReference(m.id, m.version)
                        }
                    ).also {
                            convertedMessages += it
                        }
                }

                is ObservedEnvironmentResponseMessage -> {
                    val receivedBy = IncomingInterface(
                        IncomingInterfaceId(
                            Generators.nameBasedGenerator()
                                .generate("$versionId:IncomingInterface:${message.receivedBy.protocol}:${message.receivedBy.protocolData}")
                        ),
                        Protocol(message.receivedBy.protocol.value),
                        ProtocolData(message.receivedBy.protocolData.data)
                    )
                    val m = convertedMessages[message.reactionTo.order] as ComponentResponseMessage
                    EnvironmentResponseMessage(
                        EnvironmentResponseMessageId(UUID.randomUUID()),
                        MessageValue(message.value.value),
                        convertedMessages.size,
                        EntityReference(receivedBy.id, null),
                        EntityReference(m.id, m.version)
                    ).also {
                        convertedMessages += it
                    }
                }
            }
        }
        return sortedSetOf(*convertedMessages.toTypedArray())
    }
}