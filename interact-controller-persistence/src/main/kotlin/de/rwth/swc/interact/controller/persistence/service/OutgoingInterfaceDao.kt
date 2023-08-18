package de.rwth.swc.interact.controller.persistence.service

import de.rwth.swc.interact.controller.persistence.domain.OutgoingInterfaceEntityNoRelations
import de.rwth.swc.interact.controller.persistence.domain.toEntity
import de.rwth.swc.interact.controller.persistence.repository.OutgoingInterfaceRepository
import de.rwth.swc.interact.domain.InterfaceId
import de.rwth.swc.interact.domain.MessageId
import de.rwth.swc.interact.domain.OutgoingInterface
import org.springframework.data.neo4j.core.Neo4jTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service to access OutgoingInterfaceEntity needed to support Kotlin value classes and to hide the repository
 */
interface OutgoingInterfaceDao {
    fun save(outgoingInterface: OutgoingInterface): InterfaceId
    fun findByMessage(messageId: MessageId): InterfaceId?
}

@Service
@Transactional
internal class OutgoingInterfaceDaoImpl(
    private val neo4jTemplate: Neo4jTemplate,
    private val outgoingInterfaceRepository: OutgoingInterfaceRepository
) : OutgoingInterfaceDao {
    override fun save(outgoingInterface: OutgoingInterface): InterfaceId {
        return InterfaceId(
            neo4jTemplate.saveAs(
                outgoingInterface.toEntity(),
                OutgoingInterfaceEntityNoRelations::class.java
            ).id
        )
    }

    override fun findByMessage(messageId: MessageId): InterfaceId? {
        return outgoingInterfaceRepository.findByMessage(messageId.id)?.let { InterfaceId(it.id) }
    }
}