package de.rwth.swc.interact.test

import java.util.*


class PropertiesBasedComponentInformationLoader : ComponentInformationLoader {

    private val props = Properties()

    init {
        props.load(this.javaClass.classLoader.getResourceAsStream("interact.properties"))
    }

    override fun getComponentName(): String = props.getProperty("component.name")

    override fun getComponentVersion(): String = props.getProperty("component.version")

}