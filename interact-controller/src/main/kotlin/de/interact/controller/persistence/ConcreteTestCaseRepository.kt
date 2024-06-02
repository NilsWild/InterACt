package de.interact.controller.persistence

import de.interact.controller.expectations.derivation.repository.TestInteractionsRepository
import de.interact.controller.persistence.domain.ConcreteTestCaseEntity
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ConcreteTestCaseRepository: org.springframework.data.repository.Repository<ConcreteTestCaseEntity, UUID>, TestInteractionsRepository