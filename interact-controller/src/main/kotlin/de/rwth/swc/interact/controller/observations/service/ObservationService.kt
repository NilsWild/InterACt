package de.rwth.swc.interact.controller.observations.service

import de.rwth.swc.interact.controller.integrations.service.IntegrationService
import de.rwth.swc.interact.controller.observations.repository.ObservationRepository
import de.rwth.swc.interact.controller.persistence.service.*
import de.rwth.swc.interact.domain.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ObservationService(
    private val componentDao: ComponentDao,
    private val abstractTestCaseDao: AbstractTestCaseDao,
    private val concreteTestCaseDao: ConcreteTestCaseDao,
    private val incomingInterfaceDao: IncomingInterfaceDao,
    private val outgoingInterfaceDao: OutgoingInterfaceDao,
    private val messageDao: MessageDao,
    private val observationRepository: ObservationRepository,
    private val integrationService: IntegrationService
) {
    fun storeObservation(component: Component) {

        val componentId = createComponentIfItDoesNotExist(component)
        val abstractTestCases = createAbstractTestCasesIfTheyDoNotExist(componentId, component.abstractTestCases)
        abstractTestCases.forEach { abstractTestCase ->
            val concreteTestCases = createConcreteTestCasesIfTheyDoNotExist(abstractTestCase.id!!, abstractTestCase.concreteTestCases)
            concreteTestCases.forEach { concreteTestCase ->
                storeMessagesIfTheyDoNotExist(componentId,concreteTestCase)
            }
        }
    }

    private fun storeMessagesIfTheyDoNotExist(componentId: ComponentId, concreteTestCase: ConcreteTestCase) {
        val messageIds = concreteTestCase.observedMessages.reversed()
            .runningFold(null) { next: MessageId?, m: Message ->
                saveMessage(componentId, m, next)
            }.filterNotNull()
        addMessagesToTestCase(concreteTestCase.id!!, messageIds.reversed())

        integrationService.updateInteractionExpectationValidationPlanInfos(
            concreteTestCase
        )
    }

    private fun createConcreteTestCasesIfTheyDoNotExist(
        abstractTestCaseId: AbstractTestCaseId,
        concreteTestCases: List<ConcreteTestCase>
    ): List<ConcreteTestCase> {
        return concreteTestCases.filter {
            concreteTestCaseDao.findByAbstractTestCaseIdAndParameters(
                abstractTestCaseId,
                it.parameters
            ) == null
        }.map {
            it.apply {
                val id = concreteTestCaseDao.findByAbstractTestCaseIdAndParameters(
                    abstractTestCaseId,
                    this.parameters
                )?.id
                if (id == null) {
                    concreteTestCaseDao.save(
                        this
                    ).let { newId ->
                        this.id = newId
                        abstractTestCaseDao.addConcreteTestCase(abstractTestCaseId, newId)
                    }
                } else {
                    this.id = id
                }
            }
        }
    }

    private fun createComponentIfItDoesNotExist(component: Component): ComponentId {
        return componentDao.findIdByNameAndVersion(component.name, component.version)
            ?: componentDao.save(component)
    }

    private fun createAbstractTestCasesIfTheyDoNotExist(
        componentId: ComponentId,
        abstractTestCases: Set<AbstractTestCase>
    ): List<AbstractTestCase> {
        return abstractTestCases.map {
            it.apply {
                val id = abstractTestCaseDao.findIdByComponentIdSourceAndName(
                    componentId,
                    this.source,
                    this.name
                )
                if (id == null) {
                    abstractTestCaseDao.save(
                        this
                    ).let { newId ->
                        this.id = newId
                        componentDao.addAbstractTestCase(componentId, newId)
                    }
                } else {
                    this.id = id
                }
            }
        }
    }

    private fun saveMessage(componentId: ComponentId, m: Message, next: MessageId?): MessageId {
        val messageId: MessageId
        when (m) {
            is ReceivedMessage -> {
                var interfaceId =
                    observationRepository.findIncomingInterfaceIdByComponentIdAndProtocolAndName(
                        componentId,
                        m.receivedBy.protocol,
                        m.receivedBy.protocolData
                    )
                if (interfaceId == null) {
                    interfaceId =
                        incomingInterfaceDao.save(
                            m.receivedBy
                        )
                    componentDao.addProvidedInterface(componentId, interfaceId)
                }
                messageId =
                    messageDao.save(
                        m
                    )
                messageDao.setReceivedBy(messageId, interfaceId)
            }
            is SentMessage -> {
                var interfaceId =
                    observationRepository.findOutgoingInterfaceIdByComponentIdAndProtocolAndName(
                        componentId,
                        m.sentBy.protocol,
                        m.sentBy.protocolData
                    )
                if (interfaceId == null) {
                    interfaceId =
                        outgoingInterfaceDao.save(
                            m.sentBy
                        )
                    componentDao.addRequiredInterface(componentId, interfaceId)
                }
                messageId =
                    messageDao.save(m)
                messageDao.setSentBy(messageId, interfaceId)
            }
        }
        if (next != null) {
            messageDao.setNext(messageId, next)
        }
        if (m.originalMessageId != null) {
            messageDao.setCopyOf(messageId, m.originalMessageId!!)
        }
        return messageId
    }

    private fun addMessagesToTestCase(concreteTestCaseId: ConcreteTestCaseId, messageIds: List<MessageId>) {
        concreteTestCaseDao.addMessages(concreteTestCaseId, messageIds)
    }
}