package de.interact.controller.persistence.domain

import de.interact.domain.shared.EntityReferenceProjection
import de.interact.domain.shared.TestDefinitionId
import org.springframework.data.neo4j.core.schema.DynamicLabels
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship

const val TEST_CASE_NODE_LABEL = "TestCase"
const val INCOMPLETE_TEST_CASE_NODE_LABEL = "IncompleteTestCase"
const val EXECUTABLE_TEST_CASE_NODE_LABEL = "ExecutableTestCase"
const val FINISHED_TEST_CASE_NODE_LABEL = "FinishedTestCase"
const val VALIDATED_TEST_CASE_NODE_LABEL = "ValidatedTestCase"
const val FAILED_TEST_CASE_NODE_LABEL = "FailedTestCase"

@Node(TEST_CASE_NODE_LABEL)
class TestCaseEntity: Entity() {

    @Relationship
    lateinit var replacements: Set<ReplacementEntity>

    @Relationship
    lateinit var derivedFrom: AbstractTestCaseEntity

    lateinit var parameters: List<String>

    @Relationship
    var actualTest: ConcreteTestCaseEntity? = null

    @DynamicLabels
    var labels: Set<String> = emptySet()

}

fun testCaseEntityReference(id: TestDefinitionId, version: Long?): TestCaseEntity {
    return TestCaseEntity().also {
        it.id = id.value
        it.version = version
    }
}

fun incompleteTestCaseEntity(id: TestDefinitionId, version: Long?, replacements: Set<ReplacementEntity>, derivedFrom: AbstractTestCaseEntity, parameters: List<String>): TestCaseEntity {
    return testCaseEntityReference(id, version).also {
        it.replacements = replacements
        it.derivedFrom = derivedFrom
        it.parameters = parameters
    }
}

fun executableTestCaseEntity(id: TestDefinitionId, version: Long?, replacements: Set<ReplacementEntity>, derivedFrom: AbstractTestCaseEntity, parameters: List<String>): TestCaseEntity {
    return incompleteTestCaseEntity(id, version, replacements, derivedFrom, parameters).also {
        it.labels = setOf(EXECUTABLE_TEST_CASE_NODE_LABEL)
    }
}

fun succeededTestCaseEntity(id: TestDefinitionId, version: Long?, replacements: Set<ReplacementEntity>, derivedFrom: AbstractTestCaseEntity, parameters: List<String>, actualTest: ConcreteTestCaseEntity): TestCaseEntity {
    return executableTestCaseEntity(id, version, replacements, derivedFrom, parameters).also {
        it.actualTest = actualTest
        it.labels = setOf(FINISHED_TEST_CASE_NODE_LABEL, VALIDATED_TEST_CASE_NODE_LABEL)
    }
}

fun failedTestCaseEntity(id: TestDefinitionId, version: Long?, replacements: Set<ReplacementEntity>, derivedFrom: AbstractTestCaseEntity, parameters: List<String>, actualTest: ConcreteTestCaseEntity): TestCaseEntity {
    return executableTestCaseEntity(id, version, replacements, derivedFrom, parameters).also {
        it.actualTest = actualTest
        it.labels = setOf(FINISHED_TEST_CASE_NODE_LABEL, FAILED_TEST_CASE_NODE_LABEL)
    }
}

interface TestCaseReferenceProjection: EntityReferenceProjection