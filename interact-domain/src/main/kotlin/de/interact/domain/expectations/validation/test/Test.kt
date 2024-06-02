package de.interact.domain.expectations.validation.test

import de.interact.domain.expectations.TestParameter
import de.interact.domain.expectations.shared.MessageValue
import de.interact.domain.expectations.validation.plan.TestCase
import de.interact.domain.shared.*
import java.util.*

data class Test(
    override val id: TestId,
    override val version: Long,
    val derivedFrom: EntityReference<AbstractTestId>,
    val parameters: List<TestParameter>,
    val triggeredMessages: SortedSet<Message>,
): Entity<TestId>()

sealed class Message : Entity<MessageId>(), Comparable<Message> {
    abstract val value: MessageValue
    abstract val order: Int

    sealed class SentMessage : Message() {
        abstract override val id: SentMessageId
        abstract val sentBy: EntityReference<OutgoingInterfaceId>
        abstract val dependsOn: Collection<ReceivedMessage>
    }

    sealed class ReceivedMessage : Message() {
        abstract override val id: ReceivedMessageId
        abstract val receivedBy: EntityReference<IncomingInterfaceId>
    }

    override fun compareTo(other: Message): Int {
        return order - other.order
    }
}

data class StimulusMessage(
    override val id: StimulusMessageId,
    override val version: Long,
    override val value: MessageValue,
    override val receivedBy: EntityReference<IncomingInterfaceId>
) : Message.ReceivedMessage() {
    override val order = 0
}

data class ComponentResponseMessage(
    override val id: ComponentResponseMessageId,
    override val version: Long,
    override val value: MessageValue,
    override val order: Int,
    override val sentBy: EntityReference<OutgoingInterfaceId>,
    override val dependsOn: Collection<ReceivedMessage>
) : Message.SentMessage()

data class EnvironmentResponseMessage(
    override val id: EnvironmentResponseMessageId,
    override val version: Long,
    override val value: MessageValue,
    override val order: Int,
    override val receivedBy: EntityReference<IncomingInterfaceId>,
    val reactionTo: ComponentResponseMessage
) : Message.ReceivedMessage()

fun Test.isEqualTo(testCase: TestCase.ExecutableTestCase): Boolean {
    return this.derivedFrom == testCase.derivedFrom && this.parameters == testCase.parameters
}

fun Message.toEntityReference() = EntityReference(id, version)

fun Message.ReceivedMessage.toEntityReference() = EntityReference(id, version)