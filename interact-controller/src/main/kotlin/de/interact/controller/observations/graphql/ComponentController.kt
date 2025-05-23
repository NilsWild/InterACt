package de.interact.controller.observations.graphql

import de.interact.controller.observations.repository.ComponentRepository
import de.interact.controller.persistence.domain.ComponentEntity
import de.interact.controller.persistence.domain.INCOMING_INTERFACE_NODE_LABEL
import de.interact.controller.persistence.domain.InterfaceEntity
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class ComponentController(private val components: ComponentRepository) {

    @QueryMapping
    fun components(): List<ComponentEntity> {
        return components.findAll()
    }

    @QueryMapping
    fun component(@Argument identifier: String): ComponentEntity? {
        return components.findByIdentifier(identifier)
    }

    @SchemaMapping(typeName = "Component")
    fun name(component: ComponentEntity): String {
        return component.identifier!!
    }

    @SchemaMapping(typeName = "Component")
    fun amountComponentVersions(component: ComponentEntity): Int {
        return component.versions.size
    }

    @SchemaMapping(typeName = "Interface")
    fun protocolData(interfaceEntity: InterfaceEntity): Set<Pair<String, String>> {
        return interfaceEntity.protocolData.entries.map { Pair(it.key, it.value) }.toSet()
    }

    @SchemaMapping(typeName = "Interface")
    fun type(interfaceEntity: InterfaceEntity): String {
        return if(interfaceEntity.labels.contains(INCOMING_INTERFACE_NODE_LABEL)) "Incoming" else "Outgoing"
    }
}