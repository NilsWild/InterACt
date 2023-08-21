package de.rwth.swc.interact.controller.persistence.domain

import org.springframework.data.neo4j.core.schema.RelationshipId
import org.springframework.data.neo4j.core.schema.RelationshipProperties
import org.springframework.data.neo4j.core.schema.TargetNode

@RelationshipProperties
internal data class MessageOrderRelationship(
    val order: Int,
    @TargetNode val message: MessageEntity
) : Comparable<MessageOrderRelationship> {
    @RelationshipId
    var id: String? = null
        private set

    override fun compareTo(other: MessageOrderRelationship): Int {
        return order - other.order
    }
}