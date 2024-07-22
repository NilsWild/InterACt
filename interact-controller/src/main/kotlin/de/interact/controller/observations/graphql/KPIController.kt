package de.interact.controller.observations.graphql

import de.interact.controller.observations.repository.ComponentRepository
import de.interact.controller.observations.repository.VersionRepository
import de.interact.controller.persistence.ConcreteTestCaseRepository
import de.interact.controller.persistence.UnitTestBasedInteractionExpectationRepository
import de.interact.controller.persistence.domain.UNIT_TEST_NODE_LABEL
import de.interact.domain.shared.TestState
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class KPIController(
    private val componentRepository: ComponentRepository,
    private val versionsRepository: VersionRepository,
    private val concreteTestCaseRepository: ConcreteTestCaseRepository,
    private val interactionExpectationRepository: UnitTestBasedInteractionExpectationRepository,
    private val unitTestBasedInteractionExpectationRepository: UnitTestBasedInteractionExpectationRepository
) {

    @QueryMapping
    fun kpis(): KPIs  {
        return KPIs()
    }

    @SchemaMapping(typeName = "KPIs", field = "componentAmount")
    fun componentAmount(): Int {
        return componentRepository.count().toInt()
    }

    @SchemaMapping(typeName = "KPIs", field = "componentVersionsAmount")
    fun componentVersionsAmount(): Int {
        return versionsRepository.count().toInt()
    }

    @SchemaMapping(typeName = "KPIs", field = "unitTestAmount")
    fun unitTestAmount(): Int {
        return concreteTestCaseRepository.countByLabelsContains(UNIT_TEST_NODE_LABEL).toInt()
    }

    @SchemaMapping(typeName = "KPIs", field = "unitTestPassedAmount")
    fun unitTestPassedAmount(): Int {
        return concreteTestCaseRepository.countByLabelsContainsAndStatus(UNIT_TEST_NODE_LABEL, TestState.TestFinishedState.Succeeded.toString()).toInt()
    }

    @SchemaMapping(typeName = "KPIs", field = "unitTestFailedAmount")
    fun unitTestFailedAmount(): Int {
        return concreteTestCaseRepository.countByLabelsContainsAndStatus(UNIT_TEST_NODE_LABEL, TestState.TestFinishedState.Failed.AssertionFailed.toString()).toInt() +
                concreteTestCaseRepository.countByLabelsContainsAndStatus(UNIT_TEST_NODE_LABEL, TestState.TestFinishedState.Failed.ExceptionFailed.toString()).toInt()
    }

    @SchemaMapping(typeName = "KPIs", field = "expectationsAmount")
    fun expectationsAmount(): Int {
        return unitTestBasedInteractionExpectationRepository.count().toInt()
    }

    @SchemaMapping(typeName = "KPIs", field = "expectationsPassedAmount")
    fun expectationsPassedAmount(): Int {
        return 0
    }

    @SchemaMapping(typeName = "KPIs", field = "expectationsPendingAmount")
    fun expectationsPendingAmount(): Int {
        return 0
    }

    @SchemaMapping(typeName = "KPIs", field = "expectationsFailedAmount")
    fun expectationsFailedAmount(): Int {
        return 0
    }

}

class KPIs