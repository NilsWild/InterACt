package de.rwth.swc.interact.controller.persistence.domain

import de.rwth.swc.interact.domain.*
import org.springframework.data.annotation.Version
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship
import java.util.*

const val ABSTRACT_TEST_CASE_NODE_LABEL = "AbstractTestCase"

@Node(ABSTRACT_TEST_CASE_NODE_LABEL)
internal data class AbstractTestCaseEntity(
    @Id
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val source: String,
) {

    @Relationship(type = "USED_TO_DERIVE")
    var concreteTestCases: Set<ConcreteTestCaseEntity> = emptySet()

    @Version
    var neo4jVersion: Long = 0
        private set

    fun concreteTestCase(
        id: UUID = UUID.randomUUID(),
        name: String,
        result: TestResult,
        mode: TestMode,
        init: (ConcreteTestCaseEntity.() -> Unit)? = null
    ) = ConcreteTestCaseEntity(id, name, result, mode).also {
        if (init != null) {
            it.init()
        }
        concreteTestCases = concreteTestCases.plusElement(it)
    }

    fun toDomain() = AbstractTestCase(
        AbstractTestCaseSource(this.source),
        AbstractTestCaseName(this.name)
    ).also { abstractTestCase ->
        abstractTestCase.id = AbstractTestCaseId(this.id)
        abstractTestCase.concreteTestCases = this.concreteTestCases.map { it.toDomain() }.toMutableList()
    }

}

internal fun AbstractTestCase.toEntity() = AbstractTestCaseEntity(
    this.id?.id ?: UUID.randomUUID(),
    this.name.name,
    this.source.source
).also { abstractTestCaseEntity ->
    abstractTestCaseEntity.concreteTestCases = this.concreteTestCases.map { it.toEntity() }.toMutableSet()
}

internal interface AbstractTestCaseEntityNoRelations {
    val id: UUID
    val name: String
    val source: String
}