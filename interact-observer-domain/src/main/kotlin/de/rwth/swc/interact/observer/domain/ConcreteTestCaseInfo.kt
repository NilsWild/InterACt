package de.rwth.swc.interact.observer.domain

import de.rwth.swc.interact.utils.TestMode
import java.util.*

data class ConcreteTestCaseInfo(
    val name: String,
    val mode: TestMode,
    val parameters: List<String>
) {
    var result: ObservedTestResult = ObservedTestResult.NOT_EXECUTED
    var observedMessages: MutableList<ObservedMessage> = mutableListOf()
    var interactionExpectationId: UUID? = null

    fun observedMessage(
        protocol: String,
        type: ObservedMessage.Type,
        protocolData: Map<String, String>,
        value: String,
        isParameter: Boolean
    ) = ObservedMessage(protocol, protocolData, type, value, isParameter).also {
        observedMessages.add(it)
    }

    val fullName: String
        get(){
            return "$name${(interactionExpectationId ?: "")}"
        }
}

enum class ObservedTestResult {
    SUCCESS,
    FAILED,
    NOT_EXECUTED
}