package de.rwth.swc.interact.junit.jupiter

import de.rwth.swc.interact.domain.*
import de.rwth.swc.interact.integrator.Integrator
import de.rwth.swc.interact.observer.TestObserver
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.testkit.engine.EngineTestKit
import java.util.*

class InterACtTestTest {

    @Test
    fun `can execute and observe testcases`() {

        Integrator.initialized = true
        Integrator.interactionTestCases = listOf(
            TestInvocationsDescriptor(
                AbstractTestCase(
                    AbstractTestCaseSource(InterACtTestCases::class.java.canonicalName),
                    AbstractTestCaseName("filled with null parameters in interaction test"),
                ),
                listOf(
                    TestInvocationDescriptor(
                        InteractionExpectationId(UUID.randomUUID()),
                        listOf(
                            MessageValue("Test3")
                        )
                    )
                )
            ),
            TestInvocationsDescriptor(
                AbstractTestCase(
                    AbstractTestCaseSource(InterACtTestCases::class.java.canonicalName),
                    AbstractTestCaseName("works with aggregators"),
                ),
                listOf(
                    TestInvocationDescriptor(
                        InteractionExpectationId(UUID.randomUUID()),
                        listOf(
                            MessageValue("{\"name\":\"Test3\", \"name2\":\"Test\"}"),
                        )
                    )
                )
            )
        )

        val events =
            EngineTestKit.engine("junit-jupiter").selectors(
                DiscoverySelectors.selectClass(InterACtTestCases::class.java)
            ).execute().testEvents()
        events.assertStatistics { stats -> stats.finished(6) }
        assertThat(TestObserver.getObservations()).hasSize(6)
        Thread.sleep(5000)
    }
}