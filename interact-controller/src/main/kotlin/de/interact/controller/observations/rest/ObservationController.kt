package de.interact.controller.observations.rest

import de.interact.domain.testtwin.api.ObservationHandler
import de.interact.domain.testtwin.api.dto.PartialComponentVersionModel
import de.interact.utils.Logging
import de.interact.utils.logger
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "ObservationController")
@RestController
@RequestMapping("/api/observations")
class ObservationController(private val observationHandler: ObservationHandler) : Logging {

    private val log = logger()

    @PostMapping
    fun storeObservations(@RequestBody observation: PartialComponentVersionModel) {
        log.info("Storing observation")
        observationHandler.mergeWithExistingComponentInfo(observation)
    }

}