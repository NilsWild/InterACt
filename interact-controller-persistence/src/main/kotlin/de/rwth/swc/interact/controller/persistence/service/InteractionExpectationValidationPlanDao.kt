package de.rwth.swc.interact.controller.persistence.service

import de.rwth.swc.interact.controller.persistence.domain.toEntity
import de.rwth.swc.interact.controller.persistence.repository.InteractionExpectationValidationPlanRepository
import de.rwth.swc.interact.domain.ComponentId
import de.rwth.swc.interact.domain.InteractionExpectationValidationPlan
import de.rwth.swc.interact.domain.InteractionExpectationValidationPlanId
import de.rwth.swc.interact.domain.TestInvocationDescriptor
import de.rwth.swc.interact.domain.serialization.SerializationConstants
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface InteractionExpectationValidationPlanDao {
    fun existsByPathInfo(pathInfo: String): Boolean
    fun save(interactionExpectationValidationPlan: InteractionExpectationValidationPlan): InteractionExpectationValidationPlanId
    fun findByTestInvocationDescriptor(testInvocationDescriptor: TestInvocationDescriptor): List<InteractionExpectationValidationPlan>
    fun findById(validationPlanId: InteractionExpectationValidationPlanId): InteractionExpectationValidationPlan
    fun setNextComponent(validationPlanId: InteractionExpectationValidationPlanId, componentId: ComponentId)
}

@Service
@Transactional
internal class InteractionExpectationValidationPlanDaoImpl(
    private val repository: InteractionExpectationValidationPlanRepository
) : InteractionExpectationValidationPlanDao {
    override fun existsByPathInfo(pathInfo: String): Boolean {
        return repository.existsByInteractionPathInfo(pathInfo)
    }

    override fun save(interactionExpectationValidationPlan: InteractionExpectationValidationPlan): InteractionExpectationValidationPlanId {
        return InteractionExpectationValidationPlanId(repository.save(interactionExpectationValidationPlan.toEntity()).id)
    }

    override fun findByTestInvocationDescriptor(testInvocationDescriptor: TestInvocationDescriptor): List<InteractionExpectationValidationPlan> {
        val td = SerializationConstants.mapper.writeValueAsString(testInvocationDescriptor)
        return repository.findByNextTest(td).map { it.toDomain() }
    }

    override fun findById(validationPlanId: InteractionExpectationValidationPlanId): InteractionExpectationValidationPlan {
        return repository.findById(validationPlanId.id).toDomain()
    }

    override fun setNextComponent(validationPlanId: InteractionExpectationValidationPlanId, componentId: ComponentId) {
        return repository.setNextComponent(validationPlanId.id, componentId.id)
    }

}

