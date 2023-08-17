package de.rwth.swc.interact.domain

import java.util.UUID

data class AbstractTestCase (val source: AbstractTestCaseSource, val name: AbstractTestCaseName) {

    var id: AbstractTestCaseId? = null
    var concreteTestCases: MutableList<ConcreteTestCase> = mutableListOf()

    fun concreteTestCase(
        name: ConcreteTestCaseName,
        mode: TestMode,
        parameters: List<TestCaseParameter>,
        init: (ConcreteTestCase.() -> Unit)? = null
    ) = ConcreteTestCase(name, mode, parameters).also {
        if (init != null) {
            it.init()
        }
        concreteTestCases.add(it)
    }
}

@JvmInline
value class AbstractTestCaseId(val id: UUID) {
    override fun toString(): String {
        return id.toString()
    }

    companion object {
        fun random() = AbstractTestCaseId(UUID.randomUUID())
    }
}

@JvmInline
value class AbstractTestCaseName(val name: String) {
    override fun toString(): String {
        return name
    }
}

@JvmInline
value class AbstractTestCaseSource(val source: String) {
    override fun toString(): String {
        return source
    }
}