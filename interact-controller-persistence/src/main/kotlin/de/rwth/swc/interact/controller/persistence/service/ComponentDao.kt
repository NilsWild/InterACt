package de.rwth.swc.interact.controller.persistence.service

import de.rwth.swc.interact.controller.persistence.domain.ComponentEntityNoRelations
import de.rwth.swc.interact.controller.persistence.domain.toEntity
import de.rwth.swc.interact.controller.persistence.repository.ComponentRepository
import de.rwth.swc.interact.domain.*
import org.springframework.data.neo4j.core.Neo4jTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service to access ComponentEntity needed to support Kotlin value classes and to hide the repository
 */
interface ComponentDao {

    fun addAbstractTestCase(componentId: ComponentId, abstractTestCaseId: AbstractTestCaseId)
    fun findIdByNameAndVersion(name: ComponentName, version: ComponentVersion): ComponentId?
    fun save(component: Component): ComponentId
    fun findAll(): List<Component>
    fun deleteById(componentId: ComponentId)
    fun deleteAll()
    fun addProvidedInterface(componentId: ComponentId, interfaceId: InterfaceId)
    fun addRequiredInterface(componentId: ComponentId, interfaceId: InterfaceId)

}

@Service
@Transactional
internal class ComponentDaoImpl(
    private val neo4jTemplate: Neo4jTemplate,
    private val componentRepository: ComponentRepository
) : ComponentDao {
    override fun addAbstractTestCase(componentId: ComponentId, abstractTestCaseId: AbstractTestCaseId) {
        componentRepository.addAbstractTestCase(componentId.id, abstractTestCaseId.id)
    }

    @Transactional(readOnly = true)
    override fun findIdByNameAndVersion(name: ComponentName, version: ComponentVersion): ComponentId? {
        return componentRepository.findIdByNameAndVersion(name.name, version.version)?.let {
            ComponentId(it)
        }
    }

    override fun save(component: Component): ComponentId {
        return ComponentId(neo4jTemplate.saveAs(component.toEntity(), ComponentEntityNoRelations::class.java).id)
    }

    @Transactional(readOnly = true)
    override fun findAll(): List<Component> {
        return componentRepository.findAll().map { it.toDomain() }
    }

    override fun deleteById(componentId: ComponentId) {
        componentRepository.deleteById(componentId.id)
    }

    override fun deleteAll() {
        componentRepository.deleteAll()
    }

    override fun addProvidedInterface(componentId: ComponentId, interfaceId: InterfaceId) {
        componentRepository.addProvidedInterface(componentId.id, interfaceId.id)
    }

    override fun addRequiredInterface(componentId: ComponentId, interfaceId: InterfaceId) {
        componentRepository.addRequiredInterface(componentId.id, interfaceId.id)
    }
}