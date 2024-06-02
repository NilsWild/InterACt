package de.interact.controller.persistence.domain

import org.springframework.data.annotation.Version
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship
import java.util.*


const val TEST_CASE_EXECUTION_REQUEST_NODE_LABEL = "TestCaseExecutionRequest"
const val BASED_ON_RELATIONSHIP_LABEL = "BASED_ON"

@Node(TEST_CASE_EXECUTION_REQUEST_NODE_LABEL)
class TestCaseExecutionRequestEntity() {

    @Id
    lateinit var id: UUID

    @Relationship(BASED_ON_RELATIONSHIP_LABEL)
    lateinit var basedOn: AbstractTestCaseEntity

    lateinit var parameters: List<String>

    @Version
    var version: Long? = null
}