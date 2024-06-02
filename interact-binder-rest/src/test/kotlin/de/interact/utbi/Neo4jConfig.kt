package de.interact.utbi

import org.neo4j.cypherdsl.core.renderer.Configuration
import org.neo4j.cypherdsl.core.renderer.Dialect
import org.springframework.context.annotation.Bean


@org.springframework.context.annotation.Configuration
class Neo4jConfig {
    @Bean
    fun cypherDslConfiguration(): Configuration {
        return Configuration.newConfig()
            .withDialect(Dialect.NEO4J_5).build()
    }
}