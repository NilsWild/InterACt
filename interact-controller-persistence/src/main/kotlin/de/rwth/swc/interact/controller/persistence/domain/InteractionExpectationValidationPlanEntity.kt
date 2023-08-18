package de.rwth.swc.interact.controller.persistence.domain

import de.rwth.swc.interact.domain.*
import io.github.projectmapk.jackson.module.kogera.jacksonObjectMapper
import org.springframework.data.annotation.Version
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import java.util.*

const val INTERACTION_EXPECTATION_VALIDATION_PLAN_NODE_LABEL = "InteractionExpectationValidationPlan"

@Node(INTERACTION_EXPECTATION_VALIDATION_PLAN_NODE_LABEL)
internal data class InteractionExpectationValidationPlanEntity(
    @Id
    val id: UUID = UUID.randomUUID(),
    var interactionPathInfo: String
) {
    var nextTest: String? = null
    var nextComponent: UUID? = null
    var testedPath: List<UUID>? = null
    var validated: Boolean = false

    @Version
    var neo4jVersion: Long = 0
        private set

    fun toDomain() = InteractionExpectationValidationPlan(
        this.interactionPathInfo,
        this.nextTest?.let { jacksonObjectMapper().readValue(it, TestInvocationDescriptor::class.java) },
        this.nextComponent?.let { ComponentId(it) },
        this.testedPath?.map { ConcreteTestCaseId(it) } ?: emptyList(),
        this.validated
    ).also {
        it.id = InteractionExpectationValidationPlanId(this.id)
    }
}

internal fun InteractionExpectationValidationPlan.toEntity() = InteractionExpectationValidationPlanEntity(
    this.id?.id ?: UUID.randomUUID(),
    this.interactionPathInfo
).also { entity ->
    entity.nextTest = this.nextTest?.let { jacksonObjectMapper().writeValueAsString(it) }
    entity.nextComponent = this.nextComponent?.id
    entity.testedPath = this.testedPath.map { it.id }
    entity.validated = this.validated
}