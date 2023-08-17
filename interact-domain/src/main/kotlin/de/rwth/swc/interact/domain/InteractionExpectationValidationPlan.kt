package de.rwth.swc.interact.domain

import java.util.UUID

data class InteractionExpectationValidationPlan (
    var interactionPathInfo: String,
    var nextTest: TestInvocationDescriptor?,
    var nextComponent: ComponentId?,
    var testedPath: List<ConcreteTestCaseId>,
    var validated: Boolean = false
        ) {
    var id: InteractionExpectationValidationPlanId? = null
}

@JvmInline
value class InteractionExpectationValidationPlanId(val id: UUID) {
    override fun toString() = id.toString()

    companion object {
        fun random() = InteractionExpectationValidationPlanId(UUID.randomUUID())
    }
}