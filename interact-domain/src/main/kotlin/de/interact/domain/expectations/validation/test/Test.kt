package de.interact.domain.expectations.validation.test

import de.interact.domain.expectations.TestParameter
import de.interact.domain.expectations.shared.MessageValue
import de.interact.domain.expectations.validation.plan.TestCase
import de.interact.domain.shared.*
import java.lang.Runtime.Version
import java.util.*

data class Test(
    override val id: TestId,
    override val version: Long,
    val testFor: EntityReference<VersionId>,
    val derivedFrom: EntityReference<AbstractTestId>,
    val parameters: List<TestParameter>,
    val triggeredMessages: SortedSet<Message<*>>,
    val status: TestState
): Entity<TestId>()

sealed class Message<ID: MessageId> : Entity<ID>(), Comparable<Message<*>> {
    abstract val value: MessageValue
    abstract val order: Int

    sealed class SentMessage : Message<SentMessageId>() {
        abstract override val id: SentMessageId
        abstract val sentBy: EntityReference<OutgoingInterfaceId>
        abstract val dependsOn: Collection<ReceivedMessage>
    }

    sealed class ReceivedMessage : Message<ReceivedMessageId>() {
        abstract override val id: ReceivedMessageId
        abstract val receivedBy: EntityReference<IncomingInterfaceId>
    }

    override fun compareTo(other: Message<*>): Int {
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