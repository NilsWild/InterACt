package de.interact.domain.testobservation.model

import java.util.*

data class ObservedBehavior(
    val observedFor: ConcreteTestCase
) {
    var messageSequence: SortedSet<Message> = sortedSetOf()
        private set

    fun addStimulus(message: MessageValue, receivedBy: IncomingInterface): StimulusMessage {
        if (messageSequence.isNotEmpty()) {
            throw IllegalStateException("Cannot add stimulus message to non-empty message sequence")
        }
        val stimulus = StimulusMessage(
            message,
            observedFor,
            receivedBy
        )
        messageSequence += stimulus
        return stimulus
    }

    fun addComponentResponse(message: MessageValue, sentBy: OutgoingInterface): ComponentResponseMessage {
        if (messageSequence.isEmpty()) {
            throw IllegalStateException("Cannot add component response message to empty message sequence")
        }
        val previous = messageSequence.last()
        val componentResponse = ComponentResponseMessage(
            message,
            observedFor,
            previous,
            sentBy
        )
        messageSequence += componentResponse
        return componentResponse
    }

    fun addEnvironmentResponse(message: MessageValue, receivedBy: IncomingInterface): EnvironmentResponseMessage {
        if (messageSequence.isEmpty()) {
            throw IllegalStateException("Cannot add environment response message to empty message sequence")
        }
        val previous = messageSequence.last()
        val environmentResponse = EnvironmentResponseMessage(
            message,
            observedFor,
            previous,
            receivedBy
        )
        messageSequence += environmentResponse
        return environmentResponse
    }

}
