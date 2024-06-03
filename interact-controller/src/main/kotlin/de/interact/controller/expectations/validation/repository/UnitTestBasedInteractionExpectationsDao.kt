package de.interact.controller.expectations.validation.repository

import de.interact.controller.persistence.domain.*
import de.interact.domain.expectations.validation.interactionexpectation.InteractionExpectation
import de.interact.domain.expectations.validation.spi.UnitTestBasedInteractionExpectations
import de.interact.domain.shared.UnitTestBasedInteractionExpectationId
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import java.util.*

interface UnitTestBasedInteractionExpectationsRepository{
    fun findExpectationById(id: UUID): UnitTestBasedInteractionExpectationProjection?
}

@Service
class UnitTestBasedInteractionExpectationsDao(
    private val repository: UnitTestBasedInteractionExpectationsRepository
): UnitTestBasedInteractionExpectations {
    override fun find(id: UnitTestBasedInteractionExpectationId): InteractionExpectation.UnitTestBasedInteractionExpectation? {
        return repository.findExpectationById(id.value)?.toDomain()
    }
}

interface UnitTestBasedInteractionExpectationProjection: UnitTestBasedInteractionExpectationReferenceProjection{
    val derivedFrom: UnitTestReferenceProjection
    val expectFrom: MessageReferenceProjection
    val expectTo: Set<InterfaceReferenceProjection>
    val requires: Set<InteractionExpectationReferenceProjection>
    val validationPlans: Set<ValidationPlanReferenceProjection>
}

fun UnitTestBasedInteractionExpectationProjection.toDomain(): InteractionExpectation.UnitTestBasedInteractionExpectation {
    return InteractionExpectation.UnitTestBasedInteractionExpectation(
        derivedFrom.toEntityReference(),
        expectFrom.toEntityReference(),
        expectTo.map { it.toEntityReference() }.toSet(),
        requires.map { it.toEntityReference() }.toSet(),
        validationPlans.map { it.toEntityReference() }.toSet(),
        UnitTestBasedInteractionExpectationId(id),
        version
    )
}