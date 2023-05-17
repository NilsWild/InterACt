package de.rwth.swc.interact.controller.persistence.repository

import de.rwth.swc.interact.controller.persistence.domain.IncomingInterface
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface IncomingInterfaceRepository : org.springframework.data.repository.Repository<IncomingInterface, UUID> {

    fun save(incomingInterface: IncomingInterface): IncomingInterface
}