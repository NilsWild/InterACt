package de.rwth.swc.interact.controller.persistence.domain

import org.springframework.data.annotation.Version
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship
import java.util.*

@Node
data class AbstractTestCase(
    @Id
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val source: String,
) {

    @Relationship(type = "USED_TO_DERIVE")
    var concreteTestCases: Set<ConcreteTestCase> = emptySet()
        private set

    @Version
    var neo4jVersion: Long = 0
        private set

    fun concreteTestCase(
        id: UUID = UUID.randomUUID(),
        name: String,
        result: ConcreteTestCase.TestResult,
        source: ConcreteTestCase.DataSource,
        init: (ConcreteTestCase.() -> Unit)? = null
    ) = ConcreteTestCase(id, name, result, source).also {
        if (init != null) {
            it.init()
        }
        concreteTestCases = concreteTestCases.plusElement(it)
    }

}