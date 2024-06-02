package de.interact.controller.persistence

import de.interact.controller.persistence.domain.UnitTestBasedInteractionExpectationEntity
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UnitTestBasedInteractionExpectationRepository:
    org.springframework.data.repository.Repository<UnitTestBasedInteractionExpectationEntity, UUID>,
    de.interact.controller.expectations.derivation.repository.UnitTestBasedInteractionExpectationsRepository,
    de.interact.controller.expectations.validation.repository.UnitTestBasedInteractionExpectationsRepository