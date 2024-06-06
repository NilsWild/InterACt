package de.interact.controller.expectations.validation.repository

import de.interact.controller.persistence.domain.IncomingInterfaceEntity
import de.interact.controller.persistence.domain.IncomingInterfaceReferenceProjection
import de.interact.domain.expectations.validation.`interface`.Interface
import de.interact.domain.expectations.validation.spi.Interfaces
import de.interact.domain.shared.IncomingInterfaceId
import de.interact.domain.shared.OutgoingInterfaceId
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import java.util.*

@Repository
interface InterfacesRepository: org.springframework.data.repository.Repository<IncomingInterfaceEntity, UUID> {
    fun findIncomingInterfacesByBoundToId(boundToId: UUID): Set<IncomingInterfaceReferenceProjection>
}

@Service
class InterfacesDao(
    private val repository: InterfacesRepository
): Interfaces {
    override fun findIncomingInterfacesBoundToOutgoingInterface(outgoingInterfaceId: OutgoingInterfaceId): Set<Interface.IncomingInterface> {
        return repository.findIncomingInterfacesByBoundToId(outgoingInterfaceId.value).map { it.toDomain() }.toSet()
    }
}

fun IncomingInterfaceReferenceProjection.toDomain(): Interface.IncomingInterface {
    return Interface.IncomingInterface(
        IncomingInterfaceId(id),
        version!!
    )
}