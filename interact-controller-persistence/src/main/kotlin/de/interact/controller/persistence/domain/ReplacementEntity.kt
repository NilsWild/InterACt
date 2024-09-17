package de.interact.controller.persistence.domain

import de.interact.domain.shared.EntityReferenceProjection
import de.interact.domain.shared.ReplacementId
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship

const val REPLACEMENT_NODE_LABEL = "Replacement"

@Node(REPLACEMENT_NODE_LABEL)
class ReplacementEntity: Entity() {

    @Relationship
    lateinit var messageToReplace: ReceivedMessageEntity

    @Relationship
    lateinit var replaceWithMessage: SentMessageEntity

}

fun replacementEntityReference(id: ReplacementId, version: Long?): ReplacementEntity {
    return ReplacementEntity().also {
        it.id = id.value
        it.version = version
    }
}

fun replacementEntity(id: ReplacementId, version: Long?, messageToReplace: ReceivedMessageEntity, replaceWithMessage: SentMessageEntity): ReplacementEntity {
    return replacementEntityReference(id,version).also {
        it.messageToReplace = messageToReplace
        it.replaceWithMessage = replaceWithMessage
    }
}

interface ReplacementReferenceProjection: EntityReferenceProjection
