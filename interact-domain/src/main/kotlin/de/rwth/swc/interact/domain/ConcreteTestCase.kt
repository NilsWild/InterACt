package de.rwth.swc.interact.domain

import java.util.*

data class ConcreteTestCase (
val name: ConcreteTestCaseName,
val mode: TestMode,
val parameters: List<TestCaseParameter>
) {
    var id: ConcreteTestCaseId? = null
    var result: TestResult = TestResult.NOT_EXECUTED
    var observedMessages: MutableList<Message> = mutableListOf()
    var interactionExpectationId: InteractionExpectationId? = null

    fun sentMessage(
        messageType: MessageType.Sent,
        value: MessageValue,
        sentBy: OutgoingInterface
    ) = SentMessage(messageType, value, sentBy).also {
        observedMessages.add(it)
    }

    fun receivedMessage(
        messageType: MessageType.Received,
        value: MessageValue,
        receivedBy: IncomingInterface,
        isParameter: Boolean = false
    ) = ReceivedMessage(messageType, value, receivedBy, isParameter).also {
        observedMessages.add(it)
    }

    val fullName: String
        get(){
            return "$name${(interactionExpectationId ?: "")}"
        }

}

enum class TestResult {
    SUCCESS,
    FAILED,
    NOT_EXECUTED
}

enum class TestMode {
    UNIT,
    INTERACTION
}

@JvmInline
value class ConcreteTestCaseId(val id: UUID) {
    override fun toString(): String {
        return id.toString()
    }

    companion object {
        fun random() = ConcreteTestCaseId(UUID.randomUUID())
    }
}


@JvmInline
value class ConcreteTestCaseName(val name: String) {
    override fun toString(): String {
        return name
    }
}

@JvmInline
value class TestCaseParameter(val value: String) {
    override fun toString(): String {
        return value
    }
}