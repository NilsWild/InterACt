package de.rwth.swc.interact.controller.systemexpectations.rest

import de.rwth.swc.interact.controller.persistence.service.ComponentDao
import de.rwth.swc.interact.controller.systemexpectations.service.SystemPropertyExpectationService
import de.rwth.swc.interact.domain.Component
import de.rwth.swc.interact.utils.Logging
import de.rwth.swc.interact.utils.logger
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "SystemPropertyExpectationController")
@RestController
@RequestMapping("/api/system-property-expectations")
class SystemPropertyExpectationController(private val service: SystemPropertyExpectationService): Logging {

    private val log = logger()

    @PostMapping
    fun storeSystemPropertyExpectation(@RequestBody component: Component) {
        log.info("Storing system property expectation")
        assert(component.systemPropertyExpectations.isNotEmpty())
        service.storeSystemPropertyExpectation(component)
    }

}