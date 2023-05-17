package de.rwth.swc.interact.controller.service

import de.rwth.swc.interact.controller.Neo4jBaseTest
import de.rwth.swc.interact.controller.observations.service.ObservationService
import de.rwth.swc.interact.controller.persistence.repository.ComponentRepository
import de.rwth.swc.interact.observer.domain.ObservedMessage
import de.rwth.swc.interact.observer.domain.ObservedTestResult
import de.rwth.swc.interact.observer.domain.componentInfo
import de.rwth.swc.interact.utils.TestMode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional(propagation = Propagation.NEVER)
internal class ObservationServiceTest : Neo4jBaseTest() {

    @Autowired
    lateinit var service: ObservationService

    @Autowired
    lateinit var componentRepository: ComponentRepository

    @Test
    fun `can store observation`() {
        val observations = listOf(
            componentInfo("test", "1.0.0") {
                abstractTestCaseInfo("de.rwth.swc.test.TestCase", "should be successfull") {
                    concreteTestCaseInfo("should be successfull 1", TestMode.UNIT, listOf("1")) {
                        observedMessage(
                            "REST",
                            ObservedMessage.Type.STIMULUS,
                            mapOf(Pair("url", "/api/test")),
                            "{value: test}",
                            false
                        )
                        observedMessage(
                            "REST",
                            ObservedMessage.Type.COMPONENT_RESPONSE,
                            mapOf(Pair("url", "/api/test")),
                            "{succees: true}",
                            false
                        )
                        result = ObservedTestResult.SUCCESS
                    }
                }
            },
            componentInfo("test", "1.0.0") {
                abstractTestCaseInfo("de.rwth.swc.test.TestCase", "should be successfull") {
                    concreteTestCaseInfo("should be successfull 2", TestMode.UNIT, listOf("2")) {
                        observedMessage(
                            "REST",
                            ObservedMessage.Type.STIMULUS,
                            mapOf(Pair("url", "/api/test")),
                            "{value: test}",
                            false
                        )
                        observedMessage(
                            "REST",
                            ObservedMessage.Type.COMPONENT_RESPONSE,
                            mapOf(Pair("url", "/api/test")),
                            "{succees: true}",
                            false
                        )
                        result = ObservedTestResult.SUCCESS
                    }
                }
            }
        )

        observations.forEach { service.storeObservation(it) }

        val c = componentRepository.findAll()
        assertThat(c).hasSize(1)
    }
}