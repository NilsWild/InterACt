package de.rwth.swc.interact.controller.persistence.domain

import de.rwth.swc.interact.domain.Component
import de.rwth.swc.interact.domain.ComponentId
import de.rwth.swc.interact.domain.ComponentName
import de.rwth.swc.interact.domain.ComponentVersion
import org.springframework.data.annotation.Version
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship
import java.util.*

const val COMPONENT_NODE_LABEL = "Component"

@Node(COMPONENT_NODE_LABEL)
internal data class ComponentEntity (
    @Id
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val version: String
) {
    @Relationship(type = "TESTED_BY")
    var abstractTestCases: Set<AbstractTestCaseEntity> = emptySet()

    @Relationship(type = "PROVIDES")
    var providedInterfaces: Set<IncomingInterfaceEntity> = emptySet()

    @Relationship(type = "REQUIRES")
    var requiredInterfaces: Set<OutgoingInterfaceEntity> = emptySet()

    @Version
    var neo4jVersion: Long = 0
        private set

    fun abstractTestCase(
        id: UUID = UUID.randomUUID(),
        name: String,
        source: String,
        init: (AbstractTestCaseEntity.() -> Unit)? = null
    ) = AbstractTestCaseEntity(id, name, source).also {
        if (init != null) {
            it.init()
        }
        abstractTestCases = abstractTestCases.plusElement(it)
    }

    fun incomingInterface(
        id: UUID = UUID.randomUUID(),
        protocol: String,
        protocolData: Map<String, String>
    ) = IncomingInterfaceEntity(id, protocol, protocolData).also {
        providedInterfaces = providedInterfaces.plusElement(it)
    }

    fun outgoingInterface(
        id: UUID = UUID.randomUUID(),
        protocol: String,
        protocolData: Map<String, String>
    ) = OutgoingInterfaceEntity(id, protocol, protocolData).also {
        requiredInterfaces = requiredInterfaces.plusElement(it)
    }

    fun toDomain() = Component(
        ComponentName(name),
        ComponentVersion(version)
    ).also { component ->
        component.id = ComponentId(id)
        component.abstractTestCases = abstractTestCases.map { it.toDomain() }.toMutableSet()
        component.providedInterfaces = providedInterfaces.map { it.toDomain() }.toMutableSet()
        component.requiredInterfaces = requiredInterfaces.map { it.toDomain() }.toMutableSet()
    }

}

internal fun componentEntity(
    id: UUID = UUID.randomUUID(),
    name: String,
    version: String,
    init: (ComponentEntity.() -> Unit)? = null
) = ComponentEntity(id, name, version).also {
    if (init != null) {
        it.init()
    }
}

internal fun Component.toEntity() = ComponentEntity(
    this.id?.id ?: UUID.randomUUID(),
    this.name.name,
    this.version.version
).also { component ->
    component.abstractTestCases = this.abstractTestCases.map { it.toEntity() }.toMutableSet()
    component.providedInterfaces = this.providedInterfaces.map { it.toEntity() }.toMutableSet()
    component.requiredInterfaces = this.requiredInterfaces.map { it.toEntity() }.toMutableSet()
}

internal interface ComponentEntityNoRelations {
    val id: UUID
    val name: String
    val version: String
}