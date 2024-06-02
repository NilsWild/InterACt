package de.interact.test

import de.interact.domain.testobservation.model.ComponentName
import de.interact.domain.testobservation.model.ComponentVersion
import java.util.*

/**
 * The PropertiesBasedComponentInformationLoader loads the component name and version from a properties file.
 */
object PropertiesBasedComponentInformationLoader : ComponentInformationLoader {

    private const val PROPERTIES_FILE_NAME = "interact.properties"

    private val props = Properties()

    init {
        props.load(this.javaClass.classLoader.getResourceAsStream(PROPERTIES_FILE_NAME))
    }

    override fun getComponentName(): ComponentName = ComponentName(props.getProperty("component.name"))

    override fun getComponentVersion(): ComponentVersion = ComponentVersion(props.getProperty("component.version"))

}