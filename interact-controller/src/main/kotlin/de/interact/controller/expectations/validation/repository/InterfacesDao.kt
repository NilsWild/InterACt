package de.interact.controller.expectations.validation.repository

import de.interact.controller.persistence.domain.IncomingInterfaceEntity
import de.interact.controller.persistence.domain.IncomingInterfaceReferenceProjection
import de.interact.controller.persistence.domain.OutgoingInterfaceEntity
import de.interact.controller.persistence.domain.OutgoingInterfaceReferenceProjection
import de.interact.domain.expectations.validation.`interface`.Interface
import de.interact.domain.expectations.validation.spi.Interfaces
import de.interact.domain.shared.IncomingInterfaceId
import de.interact.domain.shared.OutgoingInterfaceId
import de.interact.domain.shared.ReceivedMessageId
import de.interact.domain.shared.SentMessageId
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import java.util.*

@Repository
interface IncomingInterfaceRepository: org.springframework.data.repository.Repository<IncomingInterfaceEntity, UUID> {
    fun findIncomingInterfacesByBoundToId(boundToId: UUID): Set<IncomingInterfaceReferenceProjection>
    fun findIncomingInterfaceByReceivedMessagesId(receivedMessageId: UUID): IncomingInterfaceReferenceProjection
}

@Repository
interface OutgoingInterfaceRepository: org.springframework.data.repository.Repository<OutgoingInterfaceEntity, UUID> {
    fun findOutgoingInterfaceBySentMessagesId(sentMessageId: UUID): OutgoingInterfaceReferenceProjection
}

@Service
class InterfacesDao(
    private val incomingInterfaceRepository: IncomingInterfaceRepository,
    private val outgoingInterfaceRepository: OutgoingInterfaceRepository
): Interfaces {
    override fun findIncomingInterfacesBoundToOutgoingInterface(outgoingInterfaceId: OutgoingInterfaceId): Set<Interface.IncomingInterface> {
        return incomingInterfaceRepository.findIncomingInterfacesByBoundToId(outgoingInterfaceId.value).map { it.toDomain() }.toSet()
    }

    override fun findIncomingInterfaceMessageWasReceivedOn(receivedMessageId: ReceivedMessageId): Interface.IncomingInterface {
        return incomingInterfaceRepository.findIncomingInterfaceByReceivedMessagesId(receivedMessageId.value).toDomain()
    }

    override fun findOutgoingInterfaceMessageWasSentBy(sentMessageId: SentMessageId): Interface.OutgoingInterface {
        return outgoingInterfaceRepository.findOutgoingInterfaceBySentMessagesId(sentMessageId.value).toDomain()
    }
}

fun IncomingInterfaceReferenceProjection.toDomain(): Interface.IncomingInterface {
    return Interface.IncomingInterface(
        IncomingInterfaceId(id),
        version!!
    )
}

fun OutgoingInterfaceReferenceProjection.toDomain(): Interface.OutgoingInterface {
    return Interface.OutgoingInterface(
        OutgoingInterfaceId(id),
        version!!
    )
}