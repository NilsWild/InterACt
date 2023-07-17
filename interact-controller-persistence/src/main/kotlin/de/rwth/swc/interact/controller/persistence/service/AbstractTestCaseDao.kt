package de.rwth.swc.interact.controller.persistence.service

import de.rwth.swc.interact.controller.persistence.domain.AbstractTestCaseEntityNoRelations
import de.rwth.swc.interact.controller.persistence.domain.toEntity
import de.rwth.swc.interact.controller.persistence.repository.AbstractTestCaseRepository
import de.rwth.swc.interact.domain.*
import org.springframework.data.neo4j.core.Neo4jTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service to access AbstractTestCaseEntity needed to support Kotlin value classes and to hide the repository
 */
interface AbstractTestCaseDao{
    fun findIdByComponentIdSourceAndName(
        componentId: ComponentId,
        source: AbstractTestCaseSource,
        name: AbstractTestCaseName
    ): AbstractTestCaseId?

    fun save(abstractTestCase: AbstractTestCase): AbstractTestCaseId

    fun addConcreteTestCase(abstractTestCaseId: AbstractTestCaseId, id: ConcreteTestCaseId)
}

@Service
@Transactional
internal class AbstractTestCaseDaoImpl(private val neo4jTemplate: Neo4jTemplate, private val abstractTestCaseRepository: AbstractTestCaseRepository): AbstractTestCaseDao {

    @Transactional(readOnly = true)
    override fun findIdByComponentIdSourceAndName(
        componentId: ComponentId,
        source: AbstractTestCaseSource,
        name: AbstractTestCaseName
    ): AbstractTestCaseId? {
        return abstractTestCaseRepository.findIdByComponentIdSourceAndName(componentId.id, source.source, name.name)?.let {
            AbstractTestCaseId(it)
        }
    }

    override fun save(abstractTestCase: AbstractTestCase): AbstractTestCaseId {
        return AbstractTestCaseId(neo4jTemplate.saveAs(abstractTestCase.toEntity(), AbstractTestCaseEntityNoRelations::class.java).id)
    }

    override fun addConcreteTestCase(abstractTestCaseId: AbstractTestCaseId, id: ConcreteTestCaseId) {
        abstractTestCaseRepository.addConcreteTestCase(abstractTestCaseId.id, id.id)
    }
}