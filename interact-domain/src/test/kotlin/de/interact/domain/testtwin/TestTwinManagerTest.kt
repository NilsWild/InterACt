package de.interact.domain.testtwin

import de.interact.domain.shared.ComponentId
import de.interact.domain.shared.EntityReference
import de.interact.domain.shared.VersionId
import de.interact.domain.testtwin.api.dto.PartialComponentVersionModel
import de.interact.domain.testtwin.spi.Components
import de.interact.domain.testtwin.spi.InterfaceAddedEventPublisher
import de.interact.domain.testtwin.spi.TestAddedEventPublisher
import de.interact.domain.testtwin.spi.Versions
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.instancio.junit.InstancioExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(InstancioExtension::class)
@ExtendWith(MockKExtension::class)
class TestTwinManagerTest {

    private lateinit var testTwinManager: TestTwinManager

    @MockK
    private lateinit var components: Components

    @MockK
    private lateinit var versions: Versions

    @MockK
    private lateinit var interfaceAddedEventPublisher: InterfaceAddedEventPublisher

    @MockK
    private lateinit var testAddedeventPublisher: TestAddedEventPublisher

    @BeforeEach
    fun setUp() {
        testTwinManager = TestTwinManager(components, versions, interfaceAddedEventPublisher, testAddedeventPublisher)
    }

    @Test
    fun `handle an observation for a non existing component should create it`() {

        val component = slot<Component>()
        every { components.`find by id`(any()) } returns null andThenAnswer { component.captured }
        every { components.add(capture(component)) } returnsArgument 0
        every { versions.findVersionByComponentAndId(any(), any()) } returns null
        every { versions.save(any()) } returnsArgument 0
        every { versions.`for component`(any()) } answers { callOriginal() }

        //TODO
        val compId = ComponentId(UUID.randomUUID())
        val comp = Component(
            compId,
            ComponentIdentifier("test")
        )
        val observation = PartialComponentVersionModel(
            comp,
            Version(
                EntityReference(compId, null),
                VersionIdentifier("1.0.0"),
                setOf(),
                setOf(),
                setOf(),
                VersionId(UUID.randomUUID())
            )
        )
        testTwinManager.mergeWithExistingComponentInfo(observation)

        verify(exactly = 1) { versions.save(any()) }
    }

}
