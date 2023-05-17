package de.rwth.swc.interact.controller.observations.rest

import de.rwth.swc.interact.controller.observations.service.ObservationService
import de.rwth.swc.interact.observer.domain.ComponentInfo
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "ObservationController")
@RestController
@RequestMapping("/api/observations")
class ObservationController(private val observationService: ObservationService) {

    fun storeObservation(@RequestBody componentInfo: ComponentInfo) {
        observationService.storeObservation(componentInfo)
    }

    @PostMapping
    fun storeObservations(@RequestBody componentInfos: List<ComponentInfo>) {
        componentInfos.forEach { storeObservation(it) }
    }
}