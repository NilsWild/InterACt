package de.rwth.swc.interact.observer.domain

data class ComponentInfo(val name: String, val version: String) {

    var abstractTestCaseInfo: AbstractTestCaseInfo? = null

    fun abstractTestCaseInfo(source: String, name: String, init: (AbstractTestCaseInfo.() -> Unit)? = null) =
        AbstractTestCaseInfo(source, name).also {
            if (init != null) {
                it.init()
            }
            abstractTestCaseInfo = it
        }

}

fun componentInfo(name: String, version: String, init: (ComponentInfo.() -> Unit)? = null) =
    ComponentInfo(name, version).also {
        if (init != null) {
            it.init()
        }
    }