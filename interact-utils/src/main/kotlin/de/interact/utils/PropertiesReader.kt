package de.interact.utils

import java.util.*

abstract class PropertiesReader(private val fileName: String) {
    private val properties = Properties()

    init {
        val file = this::class.java.classLoader.getResourceAsStream(fileName)
        properties.load(file)
    }

    protected fun getProperty(key: String): String = properties.getProperty(key)
}