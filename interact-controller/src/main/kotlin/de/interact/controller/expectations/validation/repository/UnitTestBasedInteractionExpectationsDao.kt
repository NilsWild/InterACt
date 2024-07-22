package de.interact.controller.expectations.validation.repository

import de.interact.controller.persistence.domain.*
import de.interact.domain.expectations.validation.interactionexpectation.InteractionExpectation
import de.interact.domain.expectations.validation.spi.UnitTestBasedInteractionExpectations
import de.interact.domain.shared.*
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.stereotype.Service
import java.util.*

interface UnitTestBasedInteractionExpectationsRepository{
    fun findExpectationById(id: UUID): UnitTestBasedInteractionExpectationProjection?
    @Query("MATCH (c:$UNIT_TEST_NODE_LABEL{id:\$test}) " +
            "WITH COLLECT {CALL apoc.path.subgraphNodes(c,{relationshipFilter:\"" +
            ">$TRIGGERED_MESSAGES_RELATIONSHIP_LABEL|>$RECEIVED_BY_RELATIONSHIP_LABEL|" +
            "<$BOUNT_TO_RELATIONSHIP_LABEL|<$SENT_BY_RELATIONSHIP_LABEL|<$TRIGGERED_MESSAGES_RELATIONSHIP_LABEL\"}) " +
            "YIELD node} as nodes " +
            "WITH [n in nodes WHERE n:$MESSAGE_NODE_LABEL] AS messages " +
            "UNWIND messages as message " +
            "MATCH (message)<-[:$EXPECT_FROM_RELATIONSHIP_LABEL]-(exp)-[:$DERIVED_FROM_TEST_RELATIONSHIP_LABEL]->(s:$UNIT_TEST_NODE_LABEL) " +
            "WHERE s.id <> \$test " +
            "RETURN exp"
    )
    fun findExpectationPotentiallyDependantOn(test: UUID): Set<UnitTestBasedInteractionExpectationReferenceProjection>
    fun findByValidationPlansId(id: UUID): UnitTestBasedInteractionExpectationProjection?
    @Query("MATCH (exp:$UNIT_TEST_BASED_INTERACTION_EXPECTATION_NODE_LABEL{id:\$id}) " +
            "SET exp.status = \$status " +
            "RETURN exp")
    fun setStatus(id: UUID, status: String): UnitTestBasedInteractionExpectationProjection?
}

@Service
class UnitTestBasedInteractionExpectationsDao(
    private val repository: UnitTestBasedInteractionExpectationsRepository
): UnitTestBasedInteractionExpectations {
    override fun find(id: UnitTestBasedInteractionExpectationId): InteractionExpectation.UnitTestBasedInteractionExpectation? {
        return repository.findExpectationById(id.value)?.toDomain()
    }

    override fun findInteractionExpectationsPotentiallyDependantOn(test: EntityReference<UnitTestId>): Set<EntityReference<UnitTestBasedInteractionExpectationId>> {
        return repository.findExpectationPotentiallyDependantOn(test.id.value).map { it.toEntityReference() }.toSet()
    }

    override fun findByValidationPlansId(id: ValidationPlanId): InteractionExpectation.UnitTestBasedInteractionExpectation? {
        return repository.findByValidationPlansId(id.value)?.toDomain()
    }

    override fun setStatus(
        id: UnitTestBasedInteractionExpectationId,
        validated: InteractionExpectationStatus
    ) {
        repository.setStatus(id.value, validated.toString())
    }
}

interface UnitTestBasedInteractionExpectationProjection: UnitTestBasedInteractionExpectationReferenceProjection{
    val derivedFrom: UnitTestReferenceProjection
    val expectFrom: MessageReferenceProjection
    val expectTo: Set<InterfaceReferenceProjection>
    val requires: Set<InteractionExpectationReferenceProjection>
    val validationPlans: Set<ValidationPlanReferenceProjection>
    val status: String
}

fun UnitTestBasedInteractionExpectationProjection.toDomain(): InteractionExpectation.UnitTestBasedInteractionExpectation {
    return InteractionExpectation.UnitTestBasedInteractionExpectation(
        derivedFrom.toEntityReference(),
        expectFrom.toEntityReference(),
        expectTo.map { it.toEntityReference() }.toSet(),
        requires.map { it.toEntityReference() }.toSet(),
        validationPlans.map { it.toEntityReference() }.toSet(),
        InteractionExpectationStatus.fromString(status),
        UnitTestBasedInteractionExpectationId(id),
        version
    )
}