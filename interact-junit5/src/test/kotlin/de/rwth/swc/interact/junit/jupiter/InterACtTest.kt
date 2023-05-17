package de.rwth.swc.interact.junit.jupiter

import de.rwth.swc.interact.observer.TestObserver
import de.rwth.swc.interact.observer.domain.ObservedTestResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.testkit.engine.EngineTestKit

class InterACtTest {

    @Test
    fun `can execute and observe testcases`() {
        val events =
            EngineTestKit.engine("junit-jupiter").selectors(DiscoverySelectors.selectClass(InterACtTestCases::class.java))
                .execute().testEvents()
        events.assertStatistics { stats -> stats.finished(6) }
        assertThat(TestObserver.getObservations()).hasSize(6)
        assertThat(
            TestObserver.getObservations()
                .first { it.abstractTestCaseInfo?.name == "should fail" }.abstractTestCaseInfo?.concreteTestCaseInfo?.result
        ).isEqualTo(ObservedTestResult.FAILED)
        TestObserver.getObservations().filter { it.abstractTestCaseInfo?.name != "should fail" }.forEach {
            assertThat(it.abstractTestCaseInfo?.concreteTestCaseInfo?.result).isEqualTo(ObservedTestResult.SUCCESS)
        }
        Thread.sleep(5000)
    }
}