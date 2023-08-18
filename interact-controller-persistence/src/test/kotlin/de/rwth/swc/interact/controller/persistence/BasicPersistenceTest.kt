package de.rwth.swc.interact.controller.persistence

import de.rwth.swc.interact.controller.persistence.domain.componentEntity
import de.rwth.swc.interact.controller.persistence.repository.ComponentRepository
import de.rwth.swc.interact.domain.TestMode
import de.rwth.swc.interact.domain.TestResult
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
    internal lateinit var componentRepository: ComponentRepository

    @Autowired
    lateinit var neo4jClient: Neo4jClient

    @Test
    fun `can persist and get component`() {
        val comp = componentEntity(name = "test", version = "1.0.0") {
            val ii = incomingInterface(protocol = "REST", protocolData = mapOf(Pair("url", "/test")))
            val oi = outgoingInterface(protocol = "REST", protocolData = mapOf(Pair("url", "/test")))
            abstractTestCase(name = "test", source = "org.example.TestCaseTest") {
                concreteTestCase(
                    name = "test1",
                    result = TestResult.SUCCESS,
                    mode = TestMode.UNIT
                ) {
                    message(payload = "test", isParameter = false, receivedBy = ii)
                    message(payload = "test", isParameter = false, sentBy = oi)
                }
            }
            oi.bind("test", ii)
        }

        componentRepository.save(comp)
        val test = componentRepository.findAll()
        assertThat(test).withFailMessage("Expected 1 component, got ${test.size}").hasSize(1)
        assertThat(test.first().providedInterfaces).withFailMessage("Expected 1 provided interface, got ${test.first().providedInterfaces.size}")
            .hasSize(1)
        assertThat(test.first().requiredInterfaces).withFailMessage("Expected 1 required interface, got ${test.first().requiredInterfaces.size}")
            .hasSize(1)

        componentRepository.deleteById(comp.id)
        val nodes = neo4jClient.query("MATCH (n) RETURN n").fetch().all()
        assertThat(nodes)
            .withFailMessage("Expected no nodes in db but got ${nodes.size}").hasSize(0)

    }
}