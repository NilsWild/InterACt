package de.rwth.swc.interact.observer.domain

import de.rwth.swc.interact.utils.TestMode

data class AbstractTestCaseInfo(val source: String, val name: String) {

    var concreteTestCaseInfo: ConcreteTestCaseInfo? = null

    fun concreteTestCaseInfo(
        name: String,
        mode: TestMode,
        parameters: List<String>,
        init: (ConcreteTestCaseInfo.() -> Unit)? = null
    ) = ConcreteTestCaseInfo(name, mode, parameters).also {
        if (init != null) {
            it.init()
        }
        concreteTestCaseInfo = it
    }
}