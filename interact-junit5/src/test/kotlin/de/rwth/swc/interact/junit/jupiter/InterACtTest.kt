package de.rwth.swc.interact.junit.jupiter

import de.rwth.swc.interact.domain.AbstractTestCaseName
import de.rwth.swc.interact.domain.TestResult
import de.rwth.swc.interact.observer.TestObserver
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
                .first { it.abstractTestCases.first().name == AbstractTestCaseName("should fail") }.abstractTestCases.first().concreteTestCases.first().result
        ).isEqualTo(TestResult.FAILED)
        TestObserver.getObservations().filter { it.abstractTestCases.first().name != AbstractTestCaseName("should fail") }.forEach {
            assertThat(it.abstractTestCases.first().concreteTestCases.first().result).isEqualTo(TestResult.SUCCESS)
        }
        Thread.sleep(5000)
    }
}