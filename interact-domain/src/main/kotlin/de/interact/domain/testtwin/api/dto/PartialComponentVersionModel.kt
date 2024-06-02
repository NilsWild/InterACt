package de.interact.domain.testtwin.api.dto

import de.interact.domain.testtwin.Component
import de.interact.domain.testtwin.Version

data class PartialComponentVersionModel(
    val component: Component,
    val version: Version
) {
    init {
        require(version.versionOf.equals(component)) { "Version must be of the component" }
    }
}