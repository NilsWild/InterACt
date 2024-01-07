package de.rwth.swc.interact.controller.persistence.service

import de.rwth.swc.interact.controller.persistence.domain.IncomingInterfaceExpectationEntity
import de.rwth.swc.interact.controller.persistence.domain.InterfaceExpectationEntity
import de.rwth.swc.interact.controller.persistence.domain.OutgoingInterfaceExpectationEntity
import de.rwth.swc.interact.controller.persistence.domain.toEntity
import de.rwth.swc.interact.controller.persistence.events.InterfaceExpectationAddedEvent
import de.rwth.swc.interact.controller.persistence.repository.IncomingInterfaceExpectationRepository
import de.rwth.swc.interact.controller.persistence.repository.OutgoingInterfaceExpectationRepository
import de.rwth.swc.interact.domain.*
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.neo4j.core.Neo4jTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface InterfaceExpectationDao {
    fun findBySystemPropertyExpectationIdAndType(
        id: SystemPropertyExpectationId,
        clazz: Class<out InterfaceExpectation>
    ): InterfaceExpectation?

    fun save(expectation: InterfaceExpectation): InterfaceExpectationId
}

@Service
@Transactional
internal class InterfaceExpectationDaoImpl(
    private val neo4jTemplate: Neo4jTemplate,
    private val incomingInterfaceExpectationRepository: IncomingInterfaceExpectationRepository,
    private val outgoingInterfaceExpectationRepository: OutgoingInterfaceExpectationRepository,
    private val applicationEventPublisher: ApplicationEventPublisher
) : InterfaceExpectationDao {

    override fun findBySystemPropertyExpectationIdAndType(
        id: SystemPropertyExpectationId,
        clazz: Class<out InterfaceExpectation>
    ): InterfaceExpectation? {
        when (clazz) {
            IncomingInterfaceExpectation::class.java -> {
                return incomingInterfaceExpectationRepository.findBySystemPropertyExpectationId(id.id)
                    ?.toDomain()
            }

            OutgoingInterfaceExpectation::class.java -> {
                return outgoingInterfaceExpectationRepository.findBySystemPropertyExpectationId(id.id)
                    ?.toDomain()
            }
            else -> throw IllegalArgumentException("Unknown interface expectation type: $clazz")
        }
    }

    override fun save(expectation: InterfaceExpectation): InterfaceExpectationId {
        val entity = neo4jTemplate.save(
                expectation.toEntity()
            )
        val result = when (expectation) {
            is IncomingInterfaceExpectation -> expectation.copy()
            is OutgoingInterfaceExpectation -> expectation.copy()
        }
        result.id = InterfaceExpectationId(entity.id)
        applicationEventPublisher.publishEvent(
            InterfaceExpectationAddedEvent(this, result)
        )
        return result.id!!
    }
}