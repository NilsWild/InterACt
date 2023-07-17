package de.rwth.swc.interact.test

import de.rwth.swc.interact.domain.ComponentName
import de.rwth.swc.interact.domain.ComponentVersion

/**
 * The ComponentInformationLoader loads the component name and version.
 */
interface ComponentInformationLoader {
    fun getComponentName(): ComponentName
    fun getComponentVersion(): ComponentVersion
}

