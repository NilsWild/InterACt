package de.interact.domain.testobservation.model

import de.interact.domain.serialization.SerializationConstants
import de.interact.domain.shared.Protocol
import de.interact.domain.shared.ProtocolData
import de.interact.domain.shared.TestState
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import org.junit.jupiter.api.Test

class SerializationTest {

    @Test
    fun `can serialize and deserialize complex observation`() {
        val observation = TestObservation().apply {
            addObservedComponent(
                ComponentName("component"),
                ComponentVersion("version")
            ).apply {
                addAbstractTestCase(
                    AbstractTestCaseSource("source"),
                    AbstractTestCaseName("name"),
                    listOf(TypeIdentifier(String::class.java.canonicalName))
                ).apply {
                    addUnitTest(
                        ConcreteTestCaseName("name"),
                        listOf(TestCaseParameter("parameter"))
                    ).apply {
                        observedBehavior.addStimulus(
                            MessageValue("message"),
                            IncomingInterface(
                                Protocol("REST"),
                                ProtocolData(
                                    mapOf(
                                        "verb" to "GET",
                                        "url" to "http://localhost:8080"
                                    )
                                )
                            )
                        )
                        executionFinished(TestState.TestFinishedState.Succeeded)
                    }
                    addInteractionTest(
                        ConcreteTestCaseName("name"),
                        listOf(TestCaseParameter("parameter"))
                    ).apply {
                        observedBehavior.addStimulus(
                            MessageValue("message"),
                            IncomingInterface(
                                Protocol("REST"),
                                ProtocolData(
                                    mapOf(
                                        "verb" to "GET",
                                        "url" to "http://localhost:8080"
                                    )
                                )
                            )
                        )
                        observedBehavior.addComponentResponse(
                            MessageValue("message"),
                            OutgoingInterface(
                                Protocol("REST"),
                                ProtocolData(
                                    mapOf(
                                        "verb" to "GET",
                                        "url" to "http://localhost:8080"
                                    )
                                )
                            )
                        )
                        executionFinished(TestState.TestFinishedState.Failed.AssertionFailed)
                    }
                }
            }
        }

        val serialized = SerializationConstants.mapper.writeValueAsString(observation)
        val deserialized = SerializationConstants.mapper.readValue(serialized, TestObservation::class.java)
        deserialized shouldBeEqualToComparingFields observation
    }
}

