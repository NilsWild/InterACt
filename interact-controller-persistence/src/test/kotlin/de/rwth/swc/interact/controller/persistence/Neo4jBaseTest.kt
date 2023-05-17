package de.rwth.swc.interact.controller.persistence

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.Neo4jContainer

open class Neo4jBaseTest {

    companion object {
        private var neo4jContainer: Neo4jContainer<*>? = null

        @JvmStatic
        @BeforeAll
        fun initializeNeo4j() {
            neo4jContainer = Neo4jContainer("neo4j:5.7-community")
                .withAdminPassword("somePassword")
            neo4jContainer!!.start()
        }

        @JvmStatic
        @AfterAll
        fun stopNeo4j() {
            neo4jContainer!!.close()
        }

        @JvmStatic
        @DynamicPropertySource
        fun neo4jProperties(registry: DynamicPropertyRegistry) {
            println("" + neo4jContainer!!.httpUrl)
            println("" + neo4jContainer!!.adminPassword)
            registry.add("spring.neo4j.uri") { neo4jContainer!!.boltUrl }
            registry.add("spring.neo4j.authentication.username") { "neo4j" }
            registry.add("spring.neo4j.authentication.password") { neo4jContainer!!.adminPassword }
        }
    }
}