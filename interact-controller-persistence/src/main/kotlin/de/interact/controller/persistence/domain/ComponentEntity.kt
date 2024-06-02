package de.interact.controller.persistence.domain

import de.interact.domain.shared.ComponentId
import org.springframework.data.neo4j.core.schema.Node

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

    lateinit var identifier: String
}