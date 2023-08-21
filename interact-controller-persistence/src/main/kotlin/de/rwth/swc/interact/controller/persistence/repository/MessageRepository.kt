package de.rwth.swc.interact.controller.persistence.repository

import de.rwth.swc.interact.controller.persistence.domain.INCOMING_INTERFACE_NODE_LABEL
import de.rwth.swc.interact.controller.persistence.domain.MESSAGE_NODE_LABEL
import de.rwth.swc.interact.controller.persistence.domain.MessageEntity
import de.rwth.swc.interact.controller.persistence.domain.OUTGOING_INTERFACE_NODE_LABEL
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
internal interface MessageRepository : org.springframework.data.repository.Repository<MessageEntity, UUID> {
    fun save(message: MessageEntity): MessageEntity

    @Query(
        value = "MATCH (m:$MESSAGE_NODE_LABEL{id:\$messageId}), (ii:$INCOMING_INTERFACE_NODE_LABEL{id:\$interfaceId}) " +
                "MERGE (m)-[:RECEIVED_BY]->(ii)"
    )
    fun setReceivedBy(@Param("messageId") messageId: UUID, @Param("interfaceId") interfaceId: UUID)

    @Query(
        value = "MATCH (m:$MESSAGE_NODE_LABEL{id:\$messageId}), (oi:$OUTGOING_INTERFACE_NODE_LABEL{id:\$interfaceId}) " +
                "MERGE (m)-[:SENT_BY]->(oi)"
    )
    fun setSentBy(@Param("messageId") messageId: UUID, @Param("interfaceId") interfaceId: UUID)

    @Query(
        value = "MATCH (m1:$MESSAGE_NODE_LABEL{id:\$messageId}), (m2:$MESSAGE_NODE_LABEL{id:\$next}) " +
                "MERGE (m1)-[:NEXT]->(m2)"
    )
    fun setNext(@Param("messageId") messageId: UUID, @Param("next") next: UUID)

    @Query(
        value = "MATCH (m1:$MESSAGE_NODE_LABEL{id:\$messageId}), (m2:$MESSAGE_NODE_LABEL{id:\$copyOfId}) " +
                "MERGE (m1)-[:COPY_OF]->(m2)"
    )
    fun setCopyOf(@Param("messageId") messageId: UUID, @Param("copyOfId") copyOfId: UUID)
}