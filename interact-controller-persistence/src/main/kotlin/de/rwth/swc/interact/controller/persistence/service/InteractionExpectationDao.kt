package de.rwth.swc.interact.controller.persistence.service

import de.rwth.swc.interact.controller.persistence.repository.InteractionExpectationRepository
import de.rwth.swc.interact.domain.InteractionExpectationId
import de.rwth.swc.interact.domain.InteractionExpectationValidationPlanId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface InteractionExpectationDao {

    fun addValidationPlan(
        interactionExpectationId: InteractionExpectationId,
        validationPlanId: InteractionExpectationValidationPlanId
    )
}

@Service
@Transactional
internal class InteractionExpectationDaoImpl(private val interactionExpectationRepository: InteractionExpectationRepository) :
    InteractionExpectationDao {

    override fun addValidationPlan(
        interactionExpectationId: InteractionExpectationId,
        validationPlanId: InteractionExpectationValidationPlanId
    ) {
        return interactionExpectationRepository.addValidationPlan(interactionExpectationId, validationPlanId)
    }
}