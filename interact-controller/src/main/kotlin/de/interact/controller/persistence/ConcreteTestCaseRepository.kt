package de.interact.controller.persistence

import de.interact.controller.expectations.derivation.repository.TestInteractionsRepository
import de.interact.controller.persistence.domain.ConcreteTestCaseEntity
import de.interact.controller.persistence.domain.UNIT_TEST_NODE_LABEL
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ConcreteTestCaseRepository: org.springframework.data.repository.Repository<ConcreteTestCaseEntity, UUID>, TestInteractionsRepository {
    @Query("MATCH (n:`:#{literal(#label)}`) RETURN count(n)")
    fun countByLabelsContains(label: String): Long

    @Query("MATCH (n:`:#{literal(#label)}`) WEHRE n.status = \$status RETURN count(n)")
    fun countByLabelsContainsAndStatus(label: String, status: String): Long
}