package de.rwth.swc.interact.controller.persistence.domain

import de.rwth.swc.interact.domain.ConcreteTestCaseId
import de.rwth.swc.interact.domain.InteractionExpectationId
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship

const val INTERACTION_EXPECTATION_NODE_LABEL = "InteractionExpectation"

@Node(INTERACTION_EXPECTATION_NODE_LABEL)
internal data class InteractionExpectationEntity(
    @Id
    val id: InteractionExpectationId = InteractionExpectationId.random(),
    @Relationship(type = "EXPECT_FROM")
    val from: MessageEntity,
    @Relationship(type = "EXPECT_TO")
    val to: MessageEntity,
    var validated: Boolean = false,
    var interactionPathInfo: String?,
    var interactionPathQueue: String?,
    var nextTest: ConcreteTestCaseId?,
    var testedPath: List<ConcreteTestCaseId>?
)