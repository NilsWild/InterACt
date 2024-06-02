package de.interact.test

import de.interact.domain.testobservation.model.ComponentName
import de.interact.domain.testobservation.model.ComponentVersion

/**
 * The ComponentInformationLoader loads the component name and version.
 */
interface ComponentInformationLoader {
    fun getComponentName(): ComponentName
    fun getComponentVersion(): ComponentVersion
}

