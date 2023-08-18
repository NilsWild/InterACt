package de.rwth.swc.interact.controller.persistence.service

import de.rwth.swc.interact.controller.persistence.domain.IncomingInterfaceEntityNoRelations
import de.rwth.swc.interact.controller.persistence.domain.toEntity
import de.rwth.swc.interact.controller.persistence.repository.IncomingInterfaceRepository
import de.rwth.swc.interact.domain.IncomingInterface
import de.rwth.swc.interact.domain.InterfaceId
import org.springframework.data.neo4j.core.Neo4jTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service to access IncomingInterfaceEntity needed to support Kotlin value classes and to hide the repository
 */
interface IncomingInterfaceDao {
    fun save(incomingInterface: IncomingInterface): InterfaceId
}

@Service
@Transactional
internal class IncomingInterfaceDaoImpl(
    private val neo4jTemplate: Neo4jTemplate,
    private val incomingInterfaceRepository: IncomingInterfaceRepository
) : IncomingInterfaceDao {
    override fun save(incomingInterface: IncomingInterface): InterfaceId {
        return InterfaceId(
            neo4jTemplate.saveAs(
                incomingInterface.toEntity(),
                IncomingInterfaceEntityNoRelations::class.java
            ).id
        )
    }
}