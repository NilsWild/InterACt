package de.interact.controller.persistence.domain

import de.interact.domain.shared.ComponentId
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship

const val COMPONENT_NODE_LABEL = "Component"

@Node(COMPONENT_NODE_LABEL)
class ComponentEntity(): Entity() {

    constructor(id: ComponentId,version: Long? = null): this() {
        this.id = id.value
        this.version = version
    }

    constructor(id: ComponentId, identifier: String, version: Long? = null) : this(id,version) {
        this.identifier = identifier
    }

    var identifier: String? = null

    @Relationship(type = VERSION_OF_RELATIONSHIP_LABEL, direction = Relationship.Direction.INCOMING)
    var versions: Set<VersionEntity> = emptySet()
}