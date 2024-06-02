package de.interact.test

import de.interact.utils.PropertiesReader

object ComponentInfoProperties : PropertiesReader("component-info.properties") {
    fun getComponentName(): String = getProperty("component.name")
    fun getComponentVersion(): String = getProperty("component.version")
}