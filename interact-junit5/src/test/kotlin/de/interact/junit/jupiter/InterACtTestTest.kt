package de.interact.junit.jupiter

import de.interact.domain.shared.AbstractTestId
import de.interact.domain.testexecution.TestCaseParameter
import de.interact.domain.testexecution.TestInvocationDescriptor
import de.interact.domain.testobservation.ObservationToTwinMapper
import de.interact.domain.testobservation.config.Configuration
import de.interact.domain.testobservation.model.*
import de.interact.domain.testobservation.service.TestObservationManager
import de.interact.domain.testobservation.sp.SimpleTestObservationContextManager
import de.interact.domain.testobservation.spi.ObservationPublisher
import de.interact.integrator.Integrator
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Test
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory

internal class InterACtTestTest {

    init {
        Integrator.initialized = true
    }

    @Test
    fun `can execute and observe testcases`() {

        val observationCapture = slot<TestObservation>()
        val publisher = mockk<ObservationPublisher> {
            every { publish(capture(observationCapture)) } returns true
        }
        Configuration.observationManager =
            TestObservationManager(mutableListOf(), SimpleTestObservationContextManager(), publisher)

        Integrator.interactionTestCases = listOf(
            TestInvocationDescriptor(
                AbstractTestId(
                    ObservationToTwinMapper.abstractTestCaseId(
                        ObservationToTwinMapper.versionId(
                            ObservationToTwinMapper.componentId(ComponentName("interact-junit")),
                            ComponentVersion("1.0.0")
                        ),
                        AbstractTestCaseSource(InterACtTestCases::class.java.canonicalName),
                        AbstractTestCaseName("test with expected result for unit test")
                    ).value
                ),
                listOf(
                    TestCaseParameter("Test"),
                    TestCaseParameter(null)
                )
            ),
            TestInvocationDescriptor(
                AbstractTestId(
                    ObservationToTwinMapper.abstractTestCaseId(
                        ObservationToTwinMapper.versionId(
                            ObservationToTwinMapper.componentId(ComponentName("interact-junit")),
                            ComponentVersion("1.0.0")
                        ),
                        AbstractTestCaseSource(InterACtTestCases::class.java.canonicalName),
                        AbstractTestCaseName("test")
                    ).value
                ),
                listOf(
                    TestCaseParameter("{\"path\":\"/path/1\",\"parameters\":{\"param\":\"value\"},\"headers\":{\"header\":\"value\"},\"body\":{\"value\":{\"name\":\"a\",\"age\":1}}}")
                )
            )
        )

        LauncherFactory.create().execute(
            LauncherDiscoveryRequestBuilder
                .request()
                .selectors(
                    DiscoverySelectors.selectMethod(
                        InterACtTestCases::class.java,
                        "test with expected result for unit test",
                        String::class.java, Boolean::class.javaObjectType
                    )
                ).build()
        )

        assertSoftly(observationCapture.captured) {
            observedComponents.size shouldBe 1
            assertSoftly(observedComponents.first()) {
                testedBy.size shouldBe 1
                assertSoftly(testedBy.first()) {
                    templateFor.size shouldBe 2
                    val testCases = templateFor.iterator()
                    testCases.next() should beInstanceOf<UnitTestCase>()
                    testCases.next() should beInstanceOf<InteractionTestCase>()
                }
            }
        }
    }
}
