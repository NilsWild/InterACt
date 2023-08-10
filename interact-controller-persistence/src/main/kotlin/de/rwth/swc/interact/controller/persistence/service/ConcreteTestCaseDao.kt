package de.rwth.swc.interact.controller.persistence.service

import de.rwth.swc.interact.controller.persistence.domain.ConcreteTestCaseEntityNoRelations
import de.rwth.swc.interact.controller.persistence.domain.toEntity
import de.rwth.swc.interact.controller.persistence.repository.ConcreteTestCaseRepository
import de.rwth.swc.interact.domain.*
import org.springframework.data.neo4j.core.Neo4jTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service to access ConcreteTestCaseEntity needed to support Kotlin value classes and to hide the repository
 */
interface ConcreteTestCaseDao {
    fun findIdByAbstractTestCaseIdAndNameAndMode(
        abstractTestCaseId: AbstractTestCaseId,
        name: ConcreteTestCaseName,
        mode: TestMode
    ): ConcreteTestCaseId?
    fun save(concreteTestCase: ConcreteTestCase): ConcreteTestCaseId
    fun addMessages(concreteTestCaseId: ConcreteTestCaseId, messageIds: Collection<MessageId>)
    fun findById(id: ConcreteTestCaseId): ConcreteTestCase?
}

@Service
@Transactional
internal class ConcreteTestCaseDaoImpl(private val neo4jTemplate: Neo4jTemplate, private val concreteTestCaseRepository: ConcreteTestCaseRepository) : ConcreteTestCaseDao {
    @Transactional(readOnly = true)
    override fun findIdByAbstractTestCaseIdAndNameAndMode(
        abstractTestCaseId: AbstractTestCaseId,
        name: ConcreteTestCaseName,
        mode: TestMode
    ): ConcreteTestCaseId? {
        return concreteTestCaseRepository.findIdByAbstractTestCaseIdAndNameAndMode(
            abstractTestCaseId.id,
            name.name,
            mode
        )?.let {
            ConcreteTestCaseId(it)
        }
    }

    override fun save(concreteTestCase: ConcreteTestCase): ConcreteTestCaseId {
        return ConcreteTestCaseId(
            neo4jTemplate.saveAs(
                concreteTestCase.toEntity(),
                ConcreteTestCaseEntityNoRelations::class.java
            ).id
        )
    }

    override fun addMessages(concreteTestCaseId: ConcreteTestCaseId, messageIds: Collection<MessageId>) {
        concreteTestCaseRepository.addMessages(concreteTestCaseId.id, messageIds.map { it.id })
    }

    override fun findById(id: ConcreteTestCaseId): ConcreteTestCase? {
        return concreteTestCaseRepository.findById(id.id)?.let {
            return it.toDomain()
        }
    }
}