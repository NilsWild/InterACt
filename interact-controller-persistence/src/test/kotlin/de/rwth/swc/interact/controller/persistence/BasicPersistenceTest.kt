package de.rwth.swc.interact.controller.persistence

import de.rwth.swc.interact.controller.persistence.domain.ConcreteTestCase
import de.rwth.swc.interact.controller.persistence.domain.component
import de.rwth.swc.interact.controller.persistence.repository.ComponentRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional


@DataNeo4jTest
@EnableAutoConfiguration
@ContextConfiguration(classes = [ComponentRepository::class])
@Transactional(propagation = Propagation.NEVER)
class BasicPersistenceTest : Neo4jBaseTest() {

    @Autowired
    lateinit var componentRepository: ComponentRepository

    @Autowired
    lateinit var neo4jClient: Neo4jClient

    @Test
    fun `can persist and get component`() {
        val comp = component(name = "test", version = "1.0.0") {
            val ii = incomingInterface(protocol = "REST", protocolData = mapOf(Pair("url", "/test")))
            val oi = outgoingInterface(protocol = "REST", protocolData = mapOf(Pair("url", "/test")))
            abstractTestCase(name = "test", src = "org.example.TestCaseTest") {
                concreteTestCase(
                    name = "test1",
                    result = ConcreteTestCase.TestResult.SUCCESS,
                    source = ConcreteTestCase.DataSource.UNIT
                ) {
                    message(payload = "test", isParameter = false, receivedBy = ii)
                    message(payload = "test", isParameter = false, sentBy = oi)
                }
            }
            oi.bind("test", ii)
        }

        componentRepository.save(comp)
        val test = componentRepository.findAll()
        assertThat(test).hasSize(1)
        assertThat(test.first().providedInterfaces).hasSize(1)
        assertThat(test.first().requiredInterfaces).hasSize(1)

        componentRepository.deleteById(comp.id)
        assertThat(neo4jClient.query("MATCH (n) RETURN n").fetch().all()).hasSize(0)

    }
}