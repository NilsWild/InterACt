package de.rwth.swc.interact.controller.integrations.rest

import de.rwth.swc.interact.controller.integrations.service.IntegrationService
import de.rwth.swc.interact.integrator.domain.InteractionTestCases
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "IntegrationController")
@RestController
@RequestMapping("/api/integrations")
class IntegrationController(private val integrationService: IntegrationService) {

    @GetMapping("/{componentName}/{componentVersion}")
    fun getIntegrationsForComponent(
        @PathVariable("componentName") name: String,
        @PathVariable("componentVersion") version: String
    ): List<InteractionTestCases> {
        return integrationService.getIntegrationsForComponent(name, version)
    }
}