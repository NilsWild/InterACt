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
    fun save(concreteTestCase: ConcreteTestCase): ConcreteTestCaseId
    fun addMessages(concreteTestCaseId: ConcreteTestCaseId, messageIds: List<MessageId>)
    fun findById(id: ConcreteTestCaseId): ConcreteTestCase?
    fun findIdByTriggeredMessage(id: MessageId): ConcreteTestCaseId?
    fun findByAbstractTestCaseIdAndParameters(
        abstractTestCaseId: AbstractTestCaseId,
        parameters: List<TestCaseParameter>
    ): ConcreteTestCase?
}

@Service
@Transactional
internal class ConcreteTestCaseDaoImpl(
    private val neo4jTemplate: Neo4jTemplate,
    private val repository: ConcreteTestCaseRepository
) : ConcreteTestCaseDao {

    @Transactional(readOnly = true)
    override fun findByAbstractTestCaseIdAndParameters(
        abstractTestCaseId: AbstractTestCaseId,
        parameters: List<TestCaseParameter>
    ): ConcreteTestCase? {
        return repository.findByAbstractTestCaseIdAndParameters(
            abstractTestCaseId.id,
            parameters.map { it.value }
        )?.toDomain()
    }

    override fun save(concreteTestCase: ConcreteTestCase): ConcreteTestCaseId {
        return ConcreteTestCaseId(
            neo4jTemplate.saveAs(
                concreteTestCase.toEntity(),
                ConcreteTestCaseEntityNoRelations::class.java
            ).id
        )
    }

    override fun addMessages(concreteTestCaseId: ConcreteTestCaseId, messageIds: List<MessageId>) {
        repository.addMessages(concreteTestCaseId.id, messageIds.map { it.id })
    }

    override fun findById(id: ConcreteTestCaseId): ConcreteTestCase? {
        return repository.findById(id.id)?.let {
            return it.toDomain()
        }
    }

    override fun findIdByTriggeredMessage(id: MessageId): ConcreteTestCaseId? {
        return repository.findByTriggeredMessage(id.id)?.let {
            return ConcreteTestCaseId(it.id)
        }
    }
}