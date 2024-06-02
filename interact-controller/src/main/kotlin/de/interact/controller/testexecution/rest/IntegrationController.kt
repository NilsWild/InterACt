package de.interact.controller.testexecution.rest

import de.interact.domain.testexecution.TestInvocationDescriptor
import de.interact.domain.testexecution.api.Integrations
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "IntegrationController")
@RestController
@RequestMapping("/api/integrations")
class IntegrationController(private val integrationService: Integrations) {

    @Operation(operationId = "getIntegrationsForComponent")
    @GetMapping("/{componentName}/{componentVersion}")
    fun findInteractionTestsToExecuteForComponent(
        @PathVariable("componentName") name: String,
        @PathVariable("componentVersion") version: String
    ): List<TestInvocationDescriptor> {
        return integrationService.findInteractionTestsToExecuteForComponent(name, version)
    }
}