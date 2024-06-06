package de.interact.domain.testobservation.model

import de.interact.domain.shared.Protocol
import de.interact.domain.shared.ProtocolData
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import io.mockk.mockk
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class ObservedBehaviorTest {
    @ParameterizedTest
    @CsvSource(
        "message,protocol"
    )
    fun `addStimulus to empty behavior should succeed`(
        message: MessageValue,
        protocol: Protocol
    ) {
        val testCase: ConcreteTestCase = mockk()
        val observedBehavior = ObservedBehavior(testCase)

        val stimulus = observedBehavior.addStimulus(message, IncomingInterface(protocol, ProtocolData(mapOf())))

        assertSoftly(observedBehavior) {
            stimulus should beInstanceOf<StimulusMessage>()
            stimulus.previous shouldBe null
            stimulus.triggeredBy shouldBe testCase
            stimulus.value shouldBe message
            messageSequence shouldHaveSize 1
            messageSequence.first() shouldBe stimulus
        }
    }

    @ParameterizedTest
    @CsvSource(
        "message,protocol"
    )
    fun `addComponentResponse to behavior that contains a Stimulus should succeed`(
        message: MessageValue,
        protocol: Protocol
    ) {
        val testCase: ConcreteTestCase = mockk()
        val observedBehavior = ObservedBehavior(testCase)

        val stimulus = observedBehavior.addStimulus(message, IncomingInterface(protocol, ProtocolData(mapOf())))
        val componentResponse =
            observedBehavior.addComponentResponse(message, OutgoingInterface(protocol, ProtocolData(mapOf())))

        assertSoftly(observedBehavior) {
            componentResponse should beInstanceOf<ComponentResponseMessage>()
            componentResponse.dependsOn.shouldContainExactly(stimulus)
            messageSequence shouldHaveSize 2
            messageSequence.last() shouldBe componentResponse
            messageSequence.last().previous shouldBe stimulus
            messageSequence.last().previous!!.previous shouldBe null
        }
    }

    @ParameterizedTest
    @CsvSource(
        "message,protocol"
    )
    fun `addEnvironmentResponse to behavior that contains a ComponentResponse should succeed`(
        message: MessageValue,
        protocol: Protocol
    ) {
        val testCase: ConcreteTestCase = mockk()
        val observedBehavior = ObservedBehavior(testCase)

        val stimulus = observedBehavior.addStimulus(message, IncomingInterface(protocol, ProtocolData(mapOf())))
        val componentResponse =
            observedBehavior.addComponentResponse(message, OutgoingInterface(protocol, ProtocolData(mapOf())))
        val environmentResponse =
            observedBehavior.addEnvironmentResponse(message, IncomingInterface(protocol, ProtocolData(mapOf())))

        assertSoftly(observedBehavior) {
            environmentResponse should beInstanceOf<EnvironmentResponseMessage>()
            messageSequence shouldHaveSize 3
            messageSequence.last() shouldBe environmentResponse
            messageSequence.last().previous shouldBe componentResponse
            messageSequence.last().previous!!.previous shouldBe stimulus
            (messageSequence.last() as EnvironmentResponseMessage).reactionTo shouldBe componentResponse
        }
    }

    @Nested
    inner class Exception {
        @ParameterizedTest
        @CsvSource(
            "message,protocol"
        )
        fun `addStimulus to non empty Behavior should fail`(
            message: MessageValue,
            protocol: Protocol
        ) {
            val testCase: ConcreteTestCase = mockk()
            val observedBehavior = ObservedBehavior(testCase)

            observedBehavior.addStimulus(message, IncomingInterface(protocol, ProtocolData(mapOf())))

            assertThrows<IllegalStateException> {
                observedBehavior.addStimulus(message, IncomingInterface(protocol, ProtocolData(mapOf())))
            }
        }

        @ParameterizedTest
        @CsvSource(
            "message,protocol"
        )
        fun `addEnvironmentResponse to Behavior that does not contain a StimulusMessage should fail`(
            message: MessageValue,
            protocol: Protocol
        ) {
            val testCase: ConcreteTestCase = mockk()
            val observedBehavior = ObservedBehavior(testCase)

            assertThrows<IllegalStateException> {
                observedBehavior.addEnvironmentResponse(message, IncomingInterface(protocol, ProtocolData(mapOf())))
            }
        }

        @ParameterizedTest
        @CsvSource(
            "message,protocol"
        )
        fun `addEnvironmentResponse to Behavior that does not contain a ComponentResponse should fail`(
            message: MessageValue,
            protocol: Protocol
        ) {
            val testCase: ConcreteTestCase = mockk()
            val observedBehavior = ObservedBehavior(testCase)

            observedBehavior.addStimulus(message, IncomingInterface(protocol, ProtocolData(mapOf())))

            assertThrows<IllegalStateException> {
                observedBehavior.addEnvironmentResponse(message, IncomingInterface(protocol, ProtocolData(mapOf())))
            }
        }
    }

}