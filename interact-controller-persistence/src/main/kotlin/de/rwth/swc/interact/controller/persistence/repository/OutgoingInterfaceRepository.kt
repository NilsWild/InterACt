package de.rwth.swc.interact.controller.persistence.repository

import de.rwth.swc.interact.controller.persistence.domain.OutgoingInterfaceEntity
import org.springframework.stereotype.Repository
import java.util.*

@Repository
internal interface OutgoingInterfaceRepository : org.springframework.data.repository.Repository<OutgoingInterfaceEntity, UUID> {
    fun save(outgoingInterface: OutgoingInterfaceEntity): OutgoingInterfaceEntity
}