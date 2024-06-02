package de.interact.controller.persistence.domain

import de.interact.domain.shared.EntityReferenceProjection
import de.interact.domain.shared.InteractionGraphId
import org.springframework.data.annotation.Version
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship
import java.util.UUID

const val INTERACTION_GRAPH_NODE_LABEL = "InteractionGraph"
const val CONSISTS_OF_RELATIONSHIP_LABEL = "CONSISTS_OF"

@Node(INTERACTION_GRAPH_NODE_LABEL)
class InteractionGraphEntity: Entity() {

    @Relationship(type = CONSISTS_OF_RELATIONSHIP_LABEL)
    lateinit var interactions: Set<InteractionEntity>

}

interface InteractionGraphReferenceProjection: EntityReferenceProjection

fun interactionGraphEntityReference(id: InteractionGraphId, version: Long?): InteractionGraphEntity {
    return InteractionGraphEntity().also {
        it.id = id.value
        it.version = version
    }
}

fun interactionGraphEntity(id: InteractionGraphId, version: Long?, interactions: Set<InteractionEntity>): InteractionGraphEntity {
    return interactionGraphEntityReference(id, version).also {
        it.interactions = interactions
    }
}