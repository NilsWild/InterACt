package de.interact.controller.observations.repository

import de.interact.controller.persistence.domain.ComponentEntity
import de.interact.domain.shared.ComponentId
import de.interact.domain.testtwin.Component
import de.interact.domain.testtwin.ComponentIdentifier
import de.interact.domain.testtwin.spi.Components
import org.springframework.data.neo4j.core.Neo4jTemplate
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.graphql.data.GraphQlRepository
import org.springframework.stereotype.Service
import java.util.*

@GraphQlRepository(typeName = "Component")
interface ComponentRepository : org.springframework.data.repository.Repository<ComponentEntity, UUID>, Neo4jRepository<ComponentEntity, UUID>{
    fun findProjectionById(identifier: UUID): ComponentProjection?
    fun findAllBy(): List<ComponentProjection>
}

@Service
class ComponentDao(
    private val repository: ComponentRepository,
    private val neo4jTemplate: Neo4jTemplate
) : Components {
    override fun `find by id`(id: ComponentId): Component? {
        return repository.findProjectionById(id.value)?.toComponent()
    }

    override fun add(component: Component): Component {
        val entity = ComponentEntity(component.id, component.identifier.value, component.version)
        return neo4jTemplate.saveAs(entity, ComponentProjection::class.java).toComponent()
    }

    override fun all(): List<Component> {
        return repository.findAllBy().map { it.toComponent() }
    }
}

interface ComponentProjection {
    val id: UUID
    val identifier: String
    val version: Long?
}

fun ComponentProjection.toComponent(): Component {
    return Component(ComponentId(id), ComponentIdentifier(identifier), version)
}