package de.rwth.swc.interact.controller.persistence.repository

import de.rwth.swc.interact.controller.persistence.domain.OutgoingInterface
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface OutgoingInterfaceRepository : org.springframework.data.repository.Repository<OutgoingInterface, UUID> {
    fun save(outgoingInterface: OutgoingInterface): OutgoingInterface

}