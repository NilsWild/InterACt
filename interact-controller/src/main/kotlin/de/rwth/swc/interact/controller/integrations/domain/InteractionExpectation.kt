package de.rwth.swc.interact.controller.integrations.domain

import de.rwth.swc.interact.controller.persistence.domain.Message
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship
import java.util.*

@Node
data class InteractionExpectation(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Relationship(type = "EXPECT_FROM")
    val from: Message,
    @Relationship(type = "EXPECT_TO")
    val to: Message,
    var validated: Boolean = false,
    var interactionPathInfo: String?,
    var interactionPathQueue: String?,
    var nextTest: UUID?,
    var testedPath: List<UUID>?
)