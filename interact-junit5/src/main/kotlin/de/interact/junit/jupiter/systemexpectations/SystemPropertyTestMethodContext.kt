package de.interact.junit.jupiter.systemexpectations

import de.interact.domain.expectations.execution.systemexpectation.SystemExpectationBuilder
import java.lang.reflect.Method
import java.lang.reflect.Parameter

class SystemPropertyTestMethodContext(private val testMethod: Method) {

    fun hasPotentiallyValidSignature(): Boolean {
        if (testMethod.parameters.size != 3) return false
        if (testMethod.parameters[0].type != SystemExpectationBuilder::class.java) return false
        return true
    }

    fun getParameterName(parameterIndex: Int): String? {
        if (parameterIndex >= testMethod.parameters.size) {
            return null
        }
        val parameter: Parameter = testMethod.parameters[parameterIndex]
        if (!parameter.isNamePresent) {
            return null
        }
        return parameter.name
    }
}
