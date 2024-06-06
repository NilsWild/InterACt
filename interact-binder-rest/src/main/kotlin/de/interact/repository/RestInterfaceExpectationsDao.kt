package de.interact.repository

import de.interact.controller.persistence.domain.IncomingInterfaceExpectationEntity
import de.interact.controller.persistence.domain.OutgoingInterfaceExpectationEntity
import de.interact.domain.shared.IncomingInterfaceExpectationId
import de.interact.domain.shared.OutgoingInterfaceExpectationId
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import java.util.*

@Repository
interface IncomingRestInterfaceExpectationsRepository :
    org.springframework.data.repository.Repository<IncomingInterfaceExpectationEntity, UUID> {
    fun findByIdAndProtocol(id: UUID, protocol: String): IncomingRestInterfaceExpectationProjection?
    fun findByProtocol(protocol: String): Collection<IncomingRestInterfaceExpectationProjection>
}

@Repository
interface OutgoingInterfaceExpectationsRepository :
    org.springframework.data.repository.Repository<OutgoingInterfaceExpectationEntity, UUID> {
    fun findByIdAndProtocol(id: UUID, protocol: String): OutgoingRestInterfaceExpectationProjection?
    fun findByProtocol(protocol: String): Collection<OutgoingRestInterfaceExpectationProjection>

}

@Service
class RestInterfaceExpectationsDao(
    private val incomingRestInterfaceExpectationsRepository: IncomingRestInterfaceExpectationsRepository,
    private val outgoingInterfaceExpectationsRepository: OutgoingInterfaceExpectationsRepository
) {
    fun findIncomingById(id: IncomingInterfaceExpectationId): IncomingRestInterfaceExpectationProjection? {
        return incomingRestInterfaceExpectationsRepository.findByIdAndProtocol(id.id, "REST")
    }

    fun findOutgoingById(id: OutgoingInterfaceExpectationId): OutgoingRestInterfaceExpectationProjection? {
        return outgoingInterfaceExpectationsRepository.findByIdAndProtocol(id.id, "REST")
    }

    fun findAllIncoming(): Collection<IncomingRestInterfaceExpectationProjection> {
        return incomingRestInterfaceExpectationsRepository.findByProtocol("REST")
    }

    fun findAllOutgoing(): Collection<OutgoingRestInterfaceExpectationProjection> {
        return outgoingInterfaceExpectationsRepository.findByProtocol("REST")
    }
}