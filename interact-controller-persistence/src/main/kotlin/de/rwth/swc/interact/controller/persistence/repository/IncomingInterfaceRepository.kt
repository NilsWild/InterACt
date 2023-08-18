package de.rwth.swc.interact.controller.persistence.repository

import de.rwth.swc.interact.controller.persistence.domain.IncomingInterfaceEntity
import org.springframework.stereotype.Repository
import java.util.*

@Repository
internal interface IncomingInterfaceRepository :
    org.springframework.data.repository.Repository<IncomingInterfaceEntity, UUID> {

    fun save(incomingInterface: IncomingInterfaceEntity): IncomingInterfaceEntity
}