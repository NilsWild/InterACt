package de.rwth.swc.interact.controller

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
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