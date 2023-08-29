package de.rwth.swc.interact.junit.jupiter

import de.rwth.swc.interact.domain.*
import de.rwth.swc.interact.integrator.Integrator
import de.rwth.swc.interact.observer.ObservationControllerApi
import de.rwth.swc.interact.observer.TestObserver
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.vertx.core.Future
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.testkit.engine.EngineTestKit

@ExtendWith(MockKExtension::class)
internal class InterACtTestTest {

    @MockK
    lateinit var observationControllerApiMock: ObservationControllerApi

    @Test
    fun `can execute and observe testcases`() {

        TestObserver.client = observationControllerApiMock
        val observationsCapture = mutableListOf<List<Component>>()
        every {
            observationControllerApiMock.storeObservations(capture(observationsCapture))
        } answers {
            Future.succeededFuture()
        }

        Integrator.initialized = true
        Integrator.interactionTestCases = listOf(
            TestInvocationDescriptor(
                AbstractTestCase(
                    AbstractTestCaseSource(InterACtTestCases::class.java.canonicalName),
                    AbstractTestCaseName("filled with null parameters in interaction test"),
                ),
                listOf(
                    TestCaseParameter("Test"),
                    null
                )
            ),
            TestInvocationDescriptor(
                AbstractTestCase(
                    AbstractTestCaseSource(InterACtTestCases::class.java.canonicalName),
                    AbstractTestCaseName("works with aggregators"),
                ),
                listOf(
                    TestCaseParameter("{\"name\":\"Test3\", \"name2\":\"Test\"}"),
                )
            )
        )

        val events =
            EngineTestKit.engine("junit-jupiter").selectors(
                DiscoverySelectors.selectClass(InterACtTestCases::class.java)
            ).execute().testEvents()
        events.assertStatistics { stats -> stats.finished(6) }
        assertThat(observationsCapture).hasSize(6)
    }
}