package de.rwth.swc.interact.controller.persistence.domain

import de.rwth.swc.interact.domain.SystemExpectationCandidate
import de.rwth.swc.interact.domain.SystemExpectationCandidateId
import org.springframework.data.annotation.Version
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship

const val SYSTEM_EXPECTATION_CANDIDATE_NODE_LABEL = "SystemExpectationCandidate"

@Node(SYSTEM_EXPECTATION_CANDIDATE_NODE_LABEL)
internal class SystemExpectationCandidateEntity(
    @Id
    val id: SystemExpectationCandidateId,
    @Relationship(type = "EXPECT_FROM")
    val from: MessageEntity,
    @Relationship(type = "EXPECT_TO")
    val to: MessageEntity
) {
    @Relationship(type = "POTENTIALLY_VALIDATED_BY")
    var validationPlans: Set<ExpectationValidationPlanEntity> = emptySet()
    val validated: Boolean? = null
    val selected: Boolean? = null

    @Version
    var neo4jVersion: Long = 0
        private set
    fun toDomain() = SystemExpectationCandidate(
        from.toDomain(),
        to.toDomain()
    ).also {
        it.id = this.id
        it.validated = this.validated
        it.selected = this.selected
    }
}