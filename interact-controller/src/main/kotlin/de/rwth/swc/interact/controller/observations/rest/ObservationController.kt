package de.rwth.swc.interact.controller.observations.rest

import de.rwth.swc.interact.controller.observations.service.ObservationService
import de.rwth.swc.interact.domain.Component
import de.rwth.swc.interact.utils.Logging
import de.rwth.swc.interact.utils.logger
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "ObservationController")
@RestController
@RequestMapping("/api/observations")
class ObservationController(private val observationService: ObservationService): Logging {

    private val log = logger()

    fun storeObservation(@RequestBody component: Component) {
        observationService.storeObservation(component)
    }

    @PostMapping
    fun storeObservations(@RequestBody components: List<Component>) {
        log.info("Storing ${components.size} observations")
        components.forEach { storeObservation(it) }
    }
}