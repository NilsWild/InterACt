package de.interact.domain.testtwin.api

import de.interact.domain.testtwin.api.dto.PartialComponentVersionModel

interface ObservationHandler {
    fun mergeWithExistingComponentInfo(partialModel: PartialComponentVersionModel)
}