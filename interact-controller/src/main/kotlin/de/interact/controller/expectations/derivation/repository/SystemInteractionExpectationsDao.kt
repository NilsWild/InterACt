package de.interact.controller.expectations.derivation.repository

import de.interact.controller.persistence.domain.*
import de.interact.domain.expectations.derivation.interactionexpectation.InteractionExpectation
import de.interact.domain.expectations.derivation.spi.SystemInteractionExpectations
import de.interact.domain.shared.SystemInteractionExpectationId
import org.springframework.data.neo4j.core.Neo4jTemplate
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import java.util.*

@Repository
interface SystemInteractionExpectationsRepository: org.springframework.data.repository.Repository<SystemInteractionExpectationEntity, UUID> {
    fun findById(id: UUID): SystemInteractionExpectationProjection?
}

@Service
class SystemInteractionExpectationsDao(
    private val neo4jTemplate: Neo4jTemplate,
    private val systemInteractionExpectationsRepository: SystemInteractionExpectationsRepository
): SystemInteractionExpectations {
    override fun save(expectation: InteractionExpectation.SystemInteractionExpectation): InteractionExpectation.SystemInteractionExpectation {
        return neo4jTemplate.saveAs(expectation.toEntity(), SystemInteractionExpectationProjection::class.java).toDomain()
    }

    override fun find(expectationId: SystemInteractionExpectationId): InteractionExpectation.SystemInteractionExpectation? {
        return systemInteractionExpectationsRepository.findById(expectationId.value)?.toDomain()
    }
}

private fun InteractionExpectation.SystemInteractionExpectation.toEntity(): SystemInteractionExpectationEntity {
    return systemInteractionExpectationEntity(
        id,
        version,
        expectFrom.toEntity(),
        expectTo.map {
            it.toEntity()
        }.toSet(),
        requires.map {
            it.toEntity()
        }.toSet(),
        derivedFrom.toEntity()
    )
}

interface SystemInteractionExpectationProjection {
    val derivedFrom: SystemPropertyExpectationReferenceProjection
    val expectFrom: MessageReferenceProjection
    val expectTo: Set<InterfaceReferenceProjection>
    val requires: Set<InteractionExpectationReferenceProjection>
}

private fun SystemInteractionExpectationProjection.toDomain(): InteractionExpectation.SystemInteractionExpectation {
    return InteractionExpectation.SystemInteractionExpectation(
        derivedFrom.toEntityReference(),
        expectFrom.toEntityReference(),
        expectTo.map {
            it.toEntityReference()
        }.toSet(),
        requires.map {
            it.toEntityReference()
        }.toSet()
    )
}