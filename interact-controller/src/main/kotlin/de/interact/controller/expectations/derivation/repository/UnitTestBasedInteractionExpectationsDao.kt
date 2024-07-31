package de.interact.controller.expectations.derivation.repository

import de.interact.controller.persistence.domain.*
import de.interact.domain.expectations.derivation.interactionexpectation.InteractionExpectation
import de.interact.domain.expectations.derivation.spi.UnitTestBasedInteractionExpectations
import de.interact.domain.shared.InteractionExpectationStatus
import de.interact.domain.shared.UnitTestBasedInteractionExpectationId
import org.springframework.data.neo4j.core.Neo4jTemplate
import org.springframework.stereotype.Service
import java.util.*

interface UnitTestBasedInteractionExpectationsRepository {
    fun findById(id: UUID): UnitTestBasedInteractionExpectationProjection?
}

@Service
class UnitTestBasedInteractionExpectationsDao(
    private val neo4jTemplate: Neo4jTemplate,
    private val repository: UnitTestBasedInteractionExpectationsRepository
): UnitTestBasedInteractionExpectations {
    override fun save(expectation: InteractionExpectation.UnitTestBasedInteractionExpectation): InteractionExpectation.UnitTestBasedInteractionExpectation {
        return neo4jTemplate.saveAs(expectation.toEntity(), UnitTestBasedInteractionExpectationProjection::class.java).toDomain()
    }

    override fun find(expectationId: UnitTestBasedInteractionExpectationId): InteractionExpectation.UnitTestBasedInteractionExpectation? {
        return repository.findById(expectationId.value)?.toDomain()
    }
}

private fun InteractionExpectation.UnitTestBasedInteractionExpectation.toEntity(): UnitTestBasedInteractionExpectationEntity {
    return unitTestBasedInteractionExpectationEntity(
        id,
        version,
        expectFrom.toEntity(),
        expectTo.map {
            it.toEntity()
        }.toSet(),
        requires.map {
            it.toEntity()
        }.toSet(),
        derivedFrom.toEntity(),
        status.toString()
    )
}

interface UnitTestBasedInteractionExpectationProjection: UnitTestBasedInteractionExpectationReferenceProjection {
    val derivedFrom: UnitTestReferenceProjection
    val expectFrom: ComponentResponseReferenceProjection
    val expectTo: Set<IncomingInterfaceReferenceProjection>
    val requires: Set<InteractionExpectationReferenceProjection>
    val status: String
}

private fun UnitTestBasedInteractionExpectationProjection.toDomain(): InteractionExpectation.UnitTestBasedInteractionExpectation {
    return InteractionExpectation.UnitTestBasedInteractionExpectation(
        derivedFrom.toEntityReference(),
        expectFrom.toEntityReference(),
        expectTo.map {
            it.toEntityReference()
        }.toSet(),
        requires.map {
            it.toEntityReference()
        }.toSet(),
        InteractionExpectationStatus.fromString(status),
        UnitTestBasedInteractionExpectationId(id),
        version
    )
}
