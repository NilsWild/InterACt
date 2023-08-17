package de.rwth.swc.interact.controller.integrations.rest

import de.rwth.swc.interact.controller.integrations.service.IntegrationService
import de.rwth.swc.interact.domain.ComponentName
import de.rwth.swc.interact.domain.ComponentVersion
import de.rwth.swc.interact.domain.TestInvocationDescriptor
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "IntegrationController")
@RestController
@RequestMapping("/api/integrations")
class IntegrationController(private val integrationService: IntegrationService) {

    @Operation(operationId = "getIntegrationsForComponent")
    @GetMapping("/{componentName}/{componentVersion}")
    fun findInteractionTestsToExecuteForComponent(
        @PathVariable("componentName") name: ComponentName,
        @PathVariable("componentVersion") version: ComponentVersion
    ): List<TestInvocationDescriptor> {
        return integrationService.findInteractionTestsToExecuteForComponent(name, version)
    }
}