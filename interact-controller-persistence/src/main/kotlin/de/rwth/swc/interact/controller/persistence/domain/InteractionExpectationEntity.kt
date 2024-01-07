package de.rwth.swc.interact.controller.persistence.domain

import org.springframework.data.annotation.Version
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship
import java.util.*

const val INTERACTION_EXPECTATION_NODE_LABEL = "InteractionExpectation"

@Node(INTERACTION_EXPECTATION_NODE_LABEL)
internal data class InteractionExpectationEntity(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Relationship(type = "EXPECT_FROM")
    val from: MessageEntity? = null,
) {

    @Relationship(type = "EXPECT_TO")
    var to: SortedSet<MessageOrderRelationship> = sortedSetOf()
    var validated: Boolean? = null

    @Relationship(type = "POTENTIALLY_VALIDATED_BY")
    var validationPlans: Set<ExpectationValidationPlanEntity> = emptySet()

    @Version
    var neo4jVersion: Long = 0
        private set
}