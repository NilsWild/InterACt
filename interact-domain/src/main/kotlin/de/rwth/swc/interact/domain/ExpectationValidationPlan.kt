package de.rwth.swc.interact.domain

import java.util.*

data class ExpectationValidationPlan(
    var interactionPathInfo: String,
    var nextTest: TestInvocationDescriptor?,
    var nextComponent: ComponentId?,
    var testedPath: List<ConcreteTestCaseId>
) {
    var id: ExpectationValidationPlanId? = null
    var validated: Boolean? = null
}

@JvmInline
value class ExpectationValidationPlanId(val id: UUID) {
    override fun toString() = id.toString()

    companion object {
        fun random() = ExpectationValidationPlanId(UUID.randomUUID())
    }
}