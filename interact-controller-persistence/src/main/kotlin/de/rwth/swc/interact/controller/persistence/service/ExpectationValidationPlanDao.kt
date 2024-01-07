package de.rwth.swc.interact.controller.persistence.service

import de.rwth.swc.interact.controller.persistence.domain.toEntity
import de.rwth.swc.interact.controller.persistence.repository.ExpectationValidationPlanRepository
import de.rwth.swc.interact.domain.ComponentId
import de.rwth.swc.interact.domain.ExpectationValidationPlan
import de.rwth.swc.interact.domain.ExpectationValidationPlanId
import de.rwth.swc.interact.domain.TestInvocationDescriptor
import de.rwth.swc.interact.domain.serialization.SerializationConstants
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface ExpectationValidationPlanDao {
    fun existsByPathInfo(pathInfo: String): Boolean
    fun save(expectationValidationPlan: ExpectationValidationPlan): ExpectationValidationPlanId
    fun findByTestInvocationDescriptor(testInvocationDescriptor: TestInvocationDescriptor): List<ExpectationValidationPlan>
    fun findById(validationPlanId: ExpectationValidationPlanId): ExpectationValidationPlan
    fun setNextComponent(validationPlanId: ExpectationValidationPlanId, componentId: ComponentId)
}

@Service
@Transactional
internal class ExpectationValidationPlanDaoImpl(
    private val repository: ExpectationValidationPlanRepository
) : ExpectationValidationPlanDao {
    override fun existsByPathInfo(pathInfo: String): Boolean {
        return repository.existsByInteractionPathInfo(pathInfo)
    }

    override fun save(expectationValidationPlan: ExpectationValidationPlan): ExpectationValidationPlanId {
        return ExpectationValidationPlanId(repository.save(expectationValidationPlan.toEntity()).id)
    }

    override fun findByTestInvocationDescriptor(testInvocationDescriptor: TestInvocationDescriptor): List<ExpectationValidationPlan> {
        val td = SerializationConstants.mapper.writeValueAsString(testInvocationDescriptor)
        return repository.findByNextTest(td).map { it.toDomain() }
    }

    override fun findById(validationPlanId: ExpectationValidationPlanId): ExpectationValidationPlan {
        return repository.findById(validationPlanId.id).toDomain()
    }

    override fun setNextComponent(validationPlanId: ExpectationValidationPlanId, componentId: ComponentId) {
        return repository.setNextComponent(validationPlanId.id, componentId.id)
    }

}

