package de.rwth.swc.interact.controller.persistence.repository

import de.rwth.swc.interact.controller.persistence.domain.Message
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface MessageRepository : org.springframework.data.repository.Repository<Message, UUID> {
    fun save(message: Message): Message

    @Query(
        value = "MATCH (m:Message), (ii:IncomingInterface) " +
                "WHERE m.id=\$messageId AND ii.id=\$interfaceId " +
                "MERGE (m)-[:RECEIVED_BY]->(ii)"
    )
    fun setReceivedBy(@Param("messageId") messageId: UUID, @Param("interfaceId") interfaceId: UUID)

    @Query(
        value = "MATCH (m:Message), (oi:OutgoingInterface) " +
                "WHERE m.id=\$messageId AND oi.id=\$interfaceId " +
                "MERGE (m)-[:SENT_BY]->(oi)"
    )
    fun setSentBy(@Param("messageId") messageId: UUID, @Param("interfaceId") interfaceId: UUID)

    @Query(
        value = "MATCH (m1:Message), (m2:Message) " +
                "WHERE m1.id=\$messageId AND m2.id=\$next " +
                "MERGE (m1)-[:NEXT]->(m2)"
    )
    fun setNext(@Param("messageId") messageId: UUID, @Param("next") next: UUID)

    @Query(
        value = "MATCH (m1:Message), (m2:Message) " +
                "WHERE m1.id=\$messageId AND m2.id=\$copyOfId " +
                "MERGE (m1)-[:COPY_OF]->(m2)"
    )
    fun setCopyOf(@Param("messageId") messageId: UUID, @Param("copyOfId") copyOfId: UUID)
}