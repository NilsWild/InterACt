package de.rwth.swc.interact.controller.service

import de.rwth.swc.interact.controller.Neo4jBaseTest
import de.rwth.swc.interact.controller.observations.service.ObservationService
import de.rwth.swc.interact.controller.persistence.service.ComponentDao
import de.rwth.swc.interact.domain.*
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
    lateinit var componentDao: ComponentDao

    @Test
    fun `can store observation`() {
        val observations = listOf(
            component(ComponentName("test"), ComponentVersion("1.0.0")) {
                abstractTestCase(
                    AbstractTestCaseSource("de.rwth.swc.test.TestCase"),
                    AbstractTestCaseName("should be successfull")) {
                    concreteTestCase(ConcreteTestCaseName("should be successfull 1"), TestMode.UNIT, listOf()) {
                        receivedMessage(
                            MessageType.Received.STIMULUS,
                            MessageValue("{value: test}"),
                            receivedBy(
                                Protocol("REST"),
                                ProtocolData(mapOf(Pair("url", "/api/test")))
                            )
                        )
                        sentMessage(
                            MessageType.Sent.COMPONENT_RESPONSE,
                            MessageValue("{succees: true}"),
                            sentBy(
                                Protocol("REST"),
                                ProtocolData(mapOf(Pair("url", "/api/test")))
                            )
                        )
                        result = TestResult.SUCCESS
                    }
                }
            },
            component(ComponentName("test"), ComponentVersion("1.0.0")) {
                abstractTestCase(
                    AbstractTestCaseSource("de.rwth.swc.test.TestCase"),
                    AbstractTestCaseName("should be successfull")) {
                    concreteTestCase(ConcreteTestCaseName("should be successfull 1"), TestMode.UNIT, listOf()) {
                        receivedMessage(
                            MessageType.Received.STIMULUS,
                            MessageValue("{value: test}"),
                            receivedBy(
                                Protocol("REST"),
                                ProtocolData(mapOf(Pair("url", "/api/test")))
                            )
                        )
                        sentMessage(
                            MessageType.Sent.COMPONENT_RESPONSE,
                            MessageValue("{succees: true}"),
                            sentBy(
                                Protocol("REST"),
                                ProtocolData(mapOf(Pair("url", "/api/test")))
                            )
                        )
                        result = TestResult.SUCCESS
                    }
                }
            }
        )

        observations.forEach { service.storeObservation(it) }

        val c = componentDao.findAll()
        assertThat(c).hasSize(1)
    }
}