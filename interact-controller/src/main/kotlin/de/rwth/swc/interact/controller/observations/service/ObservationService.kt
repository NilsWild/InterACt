package de.rwth.swc.interact.controller.observations.service

import de.rwth.swc.interact.controller.integrations.service.IntegrationService
import de.rwth.swc.interact.controller.observations.repository.ObservationRepository
import de.rwth.swc.interact.controller.persistence.domain.*
import de.rwth.swc.interact.controller.persistence.repository.*
import de.rwth.swc.interact.observer.domain.AbstractTestCaseInfo
import de.rwth.swc.interact.observer.domain.ComponentInfo
import de.rwth.swc.interact.observer.domain.ConcreteTestCaseInfo
import de.rwth.swc.interact.observer.domain.ObservedMessage
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class ObservationService(
    private val componentRepository: ComponentRepository,
    private val abstractTestCaseRepository: AbstractTestCaseRepository,
    private val concreteTestCaseRepository: ConcreteTestCaseRepository,
    private val incomingInterfaceRepository: IncomingInterfaceRepository,
    private val outgoingInterfaceRepository: OutgoingInterfaceRepository,
    private val messageRepository: MessageRepository,
    private val observationRepository: ObservationRepository,
    private val integrationService: IntegrationService
) {
    fun storeObservation(componentInfo: ComponentInfo) {

        var componentId = createComponentIfItDoesNotExist(componentInfo)
        val abstractTestCaseId =
            createAbstractTestCaseIfItDoesNotExist(componentId, componentInfo.abstractTestCaseInfo!!)
        if (!concreteTestCaseDoesExist(
                abstractTestCaseId,
                componentInfo.abstractTestCaseInfo!!.concreteTestCaseInfo!!
            )
        ) {
            val concreteTestCaseId =
                createConcreteTestCase(abstractTestCaseId, componentInfo.abstractTestCaseInfo!!.concreteTestCaseInfo!!)
            val messageIds = componentInfo.abstractTestCaseInfo!!.concreteTestCaseInfo!!.observedMessages.reversed()
                .runningFold(null) { next: UUID?, m: ObservedMessage ->
                    saveMessage(componentId, m, next)
                }.filterNotNull()
            addMessagesToTestCase(concreteTestCaseId, messageIds)
            componentInfo.abstractTestCaseInfo!!.concreteTestCaseInfo!!.interactionExpectationId?.let {
                integrationService.updateInterfaceExpectationInfo(it, concreteTestCaseId, ConcreteTestCase.TestResult.valueOf(componentInfo.abstractTestCaseInfo!!.concreteTestCaseInfo!!.result.name))
            }
        }
    }

    private fun createComponentIfItDoesNotExist(componentInfo: ComponentInfo): UUID {
        return componentRepository.findIdByNameAndVersion(componentInfo.name, componentInfo.version)
            ?: componentRepository.save(component(name = componentInfo.name, version = componentInfo.version)).id
    }

    private fun createAbstractTestCaseIfItDoesNotExist(
        componentId: UUID,
        abstractTestCaseInfo: AbstractTestCaseInfo
    ): UUID {
        var id = abstractTestCaseRepository.findIdByComponentIdSourceAndName(
            componentId,
            abstractTestCaseInfo.source,
            abstractTestCaseInfo.name
        )
        return if (id == null) {
            id = abstractTestCaseRepository.save(
                AbstractTestCase(
                    source = abstractTestCaseInfo.source,
                    name = abstractTestCaseInfo.name
                )
            ).id
            componentRepository.addAbstractTestCase(componentId, id)
            id
        } else {
            id
        }
    }

    private fun concreteTestCaseDoesExist(
        abstractTestCaseId: UUID,
        concreteTestCaseInfo: ConcreteTestCaseInfo
    ): Boolean {
        return concreteTestCaseRepository.findIdByAbstractTestCaseIdAndNameAndSource(
            abstractTestCaseId,
            concreteTestCaseInfo.fullName,
            ConcreteTestCase.DataSource.valueOf(concreteTestCaseInfo.mode.name)
        ) != null
    }

    private fun createConcreteTestCase(abstractTestCaseId: UUID, concreteTestCaseInfo: ConcreteTestCaseInfo): UUID {
        val id = concreteTestCaseRepository.save(
            ConcreteTestCase(
                name = concreteTestCaseInfo.fullName,
                result = ConcreteTestCase.TestResult.valueOf(concreteTestCaseInfo.result.name),
                source = ConcreteTestCase.DataSource.valueOf(concreteTestCaseInfo.mode.name)
            )
        ).id
        abstractTestCaseRepository.addConcreteTestCase(abstractTestCaseId, id)
        return id
    }

    private fun saveMessage(componentId: UUID, m: ObservedMessage, next: UUID?): UUID {
        var messageId: UUID
        val labels = mutableListOf(m.type.name)
        if (m.type == ObservedMessage.Type.STIMULUS || m.type == ObservedMessage.Type.ENVIRONMENT_RESPONSE) {
            var interfaceId =
                observationRepository.findIncomingInterfaceIdByComponentIdAndProtocolAndName(
                    componentId,
                    m.protocol,
                    m.protocolData
                )
            if (interfaceId == null) {
                interfaceId =
                    incomingInterfaceRepository.save(
                        IncomingInterface(
                            protocol = m.protocol,
                            protocolData = m.protocolData
                        )
                    ).id
                componentRepository.addProvidedInterface(componentId, interfaceId)
            }
            messageId =
                messageRepository.save(Message(payload = m.value, isParameter = m.isParameter, labels = labels)).id
            messageRepository.setReceivedBy(messageId, interfaceId)
        } else {
            var interfaceId =
                observationRepository.findOutgoingInterfaceIdByComponentIdAndProtocolAndName(
                    componentId,
                    m.protocol,
                    m.protocolData
                )
            if (interfaceId == null) {
                interfaceId =
                    outgoingInterfaceRepository.save(
                        OutgoingInterface(
                            protocol = m.protocol,
                            protocolData = m.protocolData
                        )
                    ).id
                componentRepository.addRequiredInterface(componentId, interfaceId)
            }
            messageId =
                messageRepository.save(Message(payload = m.value, isParameter = m.isParameter, labels = labels)).id
            messageRepository.setSentBy(messageId, interfaceId)
        }
        if (next != null) {
            messageRepository.setNext(messageId, next)
        }
        if (m.originalMessageId != null) {
            messageRepository.setCopyOf(messageId, m.originalMessageId!!)
        }
        return messageId
    }

    private fun addMessagesToTestCase(concreteTestCaseId: UUID, messageIds: Collection<UUID>) {
        concreteTestCaseRepository.addMessages(concreteTestCaseId, messageIds)
    }
}