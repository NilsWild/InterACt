package de.rwth.swc.interact.domain

import java.util.*

class Component(val name: ComponentName, val version: ComponentVersion) {

    var id: ComponentId? = null
    var abstractTestCases: MutableSet<AbstractTestCase> = mutableSetOf()
    var providedInterfaces: MutableSet<IncomingInterface> = mutableSetOf()
    var requiredInterfaces: MutableSet<OutgoingInterface> = mutableSetOf()

    fun abstractTestCase(
        source: AbstractTestCaseSource,
        name: AbstractTestCaseName,
        init: (AbstractTestCase.() -> Unit)? = null
    ) =
        AbstractTestCase(source, name).also {
            if (init != null) {
                it.init()
            }
            abstractTestCases.add(it)
        }
}


fun component(name: ComponentName, version: ComponentVersion, init: (Component.() -> Unit)? = null) =
    Component(name, version).also {
        if (init != null) {
            it.init()
        }
    }

@JvmInline
value class ComponentId(val id: UUID) {
    override fun toString(): String {
        return id.toString()
    }

    companion object {
        fun random() = ComponentId(UUID.randomUUID())
    }
}

@JvmInline
value class ComponentName(val name: String) {
    override fun toString(): String {
        return name
    }
}

@JvmInline
value class ComponentVersion(val version: String) {
    override fun toString(): String {
        return version
    }
}