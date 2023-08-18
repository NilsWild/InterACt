package de.rwth.swc.interact.controller.persistence.service

import de.rwth.swc.interact.controller.persistence.domain.toEntity
import de.rwth.swc.interact.controller.persistence.repository.MessageRepository
import de.rwth.swc.interact.domain.InterfaceId
import de.rwth.swc.interact.domain.Message
import de.rwth.swc.interact.domain.MessageId
import org.springframework.data.neo4j.core.Neo4jTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service to access MessageEntity needed to support Kotlin value classes and to hide the repository
 */
interface MessageDao {
    fun save(message: Message): MessageId
    fun setReceivedBy(messageId: MessageId, incomingInterfaceId: InterfaceId)
    fun setSentBy(messageId: MessageId, outgoingInterfaceId: InterfaceId)
    fun setNext(messageId: MessageId, next: MessageId)
    fun setCopyOf(messageId: MessageId, copyOfId: MessageId)
}

@Service
@Transactional
internal class MessageDaoImpl(
    private val neo4jTemplate: Neo4jTemplate,
    private val messageRepository: MessageRepository
) : MessageDao {
    override fun save(message: Message): MessageId {
        return MessageId(
            neo4jTemplate.saveAs(
                message.toEntity(),
                de.rwth.swc.interact.controller.persistence.domain.MessageEntityNoRelations::class.java
            ).id
        )
    }

    override fun setReceivedBy(messageId: MessageId, incomingInterfaceId: InterfaceId) {
        messageRepository.setReceivedBy(messageId.id, incomingInterfaceId.id)
    }

    override fun setSentBy(messageId: MessageId, outgoingInterfaceId: InterfaceId) {
        messageRepository.setSentBy(messageId.id, outgoingInterfaceId.id)
    }

    override fun setNext(messageId: MessageId, next: MessageId) {
        messageRepository.setNext(messageId.id, next.id)
    }

    override fun setCopyOf(messageId: MessageId, copyOfId: MessageId) {
        messageRepository.setCopyOf(messageId.id, copyOfId.id)
    }
}