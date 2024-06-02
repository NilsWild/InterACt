package de.interact.domain.testobservation.service

import de.interact.domain.testobservation.spi.MessageObserver
import de.interact.domain.testobservation.spi.ObservationPublisher
import io.kotest.assertions.throwables.shouldThrowExactly
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

internal class TestObservationManagerTest {

    @Test
    fun `publish observations should succeed if all messages observers have finished`() {
        val observationPublisher = mockk<ObservationPublisher> {
            every { publish(any()) } returns true
        }
        val messageObserver = mockk<MessageObserver> {
            every { isFinished() } returns true
        }
        val testObservationManager = TestObservationManager(
            mutableListOf(messageObserver),
            mockk(),
            observationPublisher
        )

        testObservationManager.publishObservation()

        verify { observationPublisher.publish(any()) }
    }

    @Test
    fun `publish observations should fail if any messages observers is not finished`() {
        val observationPublisher = mockk<ObservationPublisher>()
        val messageObserver1 = mockk<MessageObserver> {
            every { isFinished() } returns true
        }
        val messageObserver2 = mockk<MessageObserver> {
            every { isFinished() } returns false
        }
        val testObservationManager = TestObservationManager(
            mutableListOf(messageObserver1, messageObserver2),
            mockk(),
            observationPublisher
        )

        shouldThrowExactly<IllegalStateException> {
            testObservationManager.publishObservation()
        }
    }
}