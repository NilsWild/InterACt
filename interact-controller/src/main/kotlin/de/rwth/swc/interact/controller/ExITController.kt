package de.rwth.swc.interact.controller

import de.rwth.swc.interact.domain.serialization.InteractModule
import io.github.projectmapk.jackson.module.kogera.KotlinModule
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@ComponentScan(basePackages = ["de.rwth.swc.interact"])
@OpenAPIDefinition(
    info = Info(
        title = "ExItController",
        description = "Controller for ExIT",
        version = "1.0.0"
    )
)
@EnableScheduling
class ExITController

fun main(args: Array<String>) {
    runApplication<ExITController>(*args)
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
    fun kotlinModule() = KotlinModule.Builder().build()

    @Bean
    fun interactModule() = InteractModule
}