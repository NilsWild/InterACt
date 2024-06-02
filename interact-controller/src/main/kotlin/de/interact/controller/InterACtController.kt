package de.interact.controller

import de.interact.domain.serialization.InteractModule
import io.github.projectmapk.jackson.module.kogera.KotlinFeature
import io.github.projectmapk.jackson.module.kogera.KotlinModule
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.neo4j.cypherdsl.core.renderer.Dialect
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories
import org.springframework.scheduling.annotation.EnableScheduling


@SpringBootApplication
@ComponentScan(nameGenerator = CustomBeanNameGenerator::class, basePackages = ["de.interact"])
@EnableNeo4jRepositories(basePackages = ["de.interact"])
@OpenAPIDefinition(
    info = Info(
        title = "InterACtController",
        description = "Controller for ExIT",
        version = "1.0.0"
    )
)
@EnableScheduling
class InterACtController

fun main(args: Array<String>) {
    runApplication<InterACtController>(*args)
}


/**
 * Configuration for the Jackson Kotlin module.
 * This replaces the default Kotlin module with the Kogera module.
 * The InteractModule is needed until full support for Kotlin value classes is added to Kogera.
 * https://github.com/FasterXML/jackson-module-kotlin/issues/650
 */
@Configuration
class KotlinModuleConfiguration {
    @Bean
    fun kotlinModule() = KotlinModule.Builder().configure(
        KotlinFeature.SingletonSupport, true
    ).build()

    @Bean
    fun interactModule() = InteractModule

    @Bean
    fun cypherDslConfiguration(): org.neo4j.cypherdsl.core.renderer.Configuration {
        return org.neo4j.cypherdsl.core.renderer.Configuration.newConfig()
            .withDialect(Dialect.NEO4J_5).build()
    }

}