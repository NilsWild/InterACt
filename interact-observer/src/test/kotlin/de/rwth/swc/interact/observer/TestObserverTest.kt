package de.rwth.swc.interact.observer

import de.rwth.swc.interact.domain.*
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.vertx.core.Future
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class TestObserverTest {

    @MockK
    lateinit var observationControllerApiMock: ObservationControllerApi


    @Test
    fun `when observation for a test case was started the observation will be stored via the observationControllerApi`() {
        TestObserver.client = observationControllerApiMock
        val observationsCapture = mutableListOf<List<Component>>()
        every { observationControllerApiMock.storeObservations(capture(observationsCapture)) } answers { Future.succeededFuture() }
        TestObserver.startObservation(
            this.javaClass,
            AbstractTestCaseName("Abstract TestCase"),
            ConcreteTestCaseName("Concrete TestCase"),
            listOf(),
            TestMode.UNIT
        )
        TestObserver.recordMessage(
            SentMessage(
                MessageType.Sent.COMPONENT_RESPONSE,
                MessageValue("Test"),
                OutgoingInterface(
                    Protocol("Test"),
                    ProtocolData(mapOf())
                )
            )
        )
        TestObserver.pushObservations()
        assertThat(observationsCapture).hasSize(1)
    }

    @Test
    fun `when observation for a test case is dropped the observation will not be stored via the observationControllerApi`() {
        TestObserver.client = observationControllerApiMock
        val observationsCapture = mutableListOf<List<Component>>()
        every { observationControllerApiMock.storeObservations(capture(observationsCapture)) } answers { Future.succeededFuture() }
        TestObserver.startObservation(
            this.javaClass,
            AbstractTestCaseName("Abstract TestCase"),
            ConcreteTestCaseName("Concrete TestCase"),
            listOf(),
            TestMode.UNIT
        )
        TestObserver.dropObservation()
        TestObserver.pushObservations()
        assertThat(observationsCapture).hasSize(1)
    }

    @Test
    fun `when a message should be recorded before a test was registered for observation the TestObserver should throw an exception`() {
        assertThatThrownBy {
            TestObserver.recordMessage(
                SentMessage(
                    MessageType.Sent.COMPONENT_RESPONSE,
                    MessageValue("Test"),
                    OutgoingInterface(
                        Protocol("Test"),
                        ProtocolData(mapOf())
                    )
                )
            )
        }.isInstanceOf(IllegalStateException::class.java)
            .hasMessage("No test was registered for observation.")
    }

    @Test
    fun `when a test result is set before a test was registered for observation the TestObserver should throw an exception`() {
        assertThatThrownBy {
            TestObserver.setTestResult(TestResult.SUCCESS)
        }.isInstanceOf(IllegalStateException::class.java)
            .hasMessage("No test was registered for observation.")
    }

}