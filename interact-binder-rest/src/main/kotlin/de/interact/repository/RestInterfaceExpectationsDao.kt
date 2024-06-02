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
    fun findByIdAndProtocol(id: UUID, protocol: String): IncomingRestInterfaceExpectation?
    fun findByProtocol(protocol: String): Collection<IncomingRestInterfaceExpectation>
}

@Repository
interface OutgoingInterfaceExpectationsRepository :
    org.springframework.data.repository.Repository<OutgoingInterfaceExpectationEntity, UUID> {
    fun findByIdAndProtocol(id: UUID, protocol: String): OutgoingRestInterfaceExpectation?
    fun findByProtocol(protocol: String): Collection<OutgoingRestInterfaceExpectation>

}

@Service
class RestInterfaceExpectationsDao(
    private val incomingRestInterfaceExpectationsRepository: IncomingRestInterfaceExpectationsRepository,
    private val outgoingInterfaceExpectationsRepository: OutgoingInterfaceExpectationsRepository
) {
    fun findIncomingById(id: IncomingInterfaceExpectationId): IncomingRestInterfaceExpectation? {
        return incomingRestInterfaceExpectationsRepository.findByIdAndProtocol(id.id, "REST")
    }

    fun findOutgoingById(id: OutgoingInterfaceExpectationId): OutgoingRestInterfaceExpectation? {
        return outgoingInterfaceExpectationsRepository.findByIdAndProtocol(id.id, "REST")
    }

    fun findAllIncoming(): Collection<IncomingRestInterfaceExpectation> {
        return incomingRestInterfaceExpectationsRepository.findByProtocol("REST")
    }

    fun findAllOutgoing(): Collection<OutgoingRestInterfaceExpectation> {
        return outgoingInterfaceExpectationsRepository.findByProtocol("REST")
    }
}