package de.interact.domain.testtwin

import de.interact.domain.shared.*
import de.interact.domain.testtwin.abstracttest.concretetest.InteractionTest
import de.interact.domain.testtwin.abstracttest.concretetest.UnitTest
import de.interact.domain.testtwin.api.ObservationHandler
import de.interact.domain.testtwin.api.dto.PartialComponentVersionModel
import de.interact.domain.testtwin.api.event.IncomingInterfaceAddedToVersionEvent
import de.interact.domain.testtwin.api.event.InteractionTestAddedEvent
import de.interact.domain.testtwin.api.event.OutgoingInterfaceAddedToVersionEvent
import de.interact.domain.testtwin.api.event.UnitTestAddedEvent
import de.interact.domain.testtwin.`interface`.IncomingInterface
import de.interact.domain.testtwin.`interface`.Interface
import de.interact.domain.testtwin.`interface`.OutgoingInterface
import de.interact.domain.testtwin.spi.*

class TestTwinManager(
    private val components: Components,
    private val versions: Versions,
    private val interfaceAddedEventPublisher: InterfaceAddedEventPublisher,
    private val testAddedEventPublisher: TestAddedEventPublisher
) : ObservationHandler {

    fun getComponents(): List<Component> {
        return components.all()
    }

    fun getVersionsForComponent(componentId: ComponentId): List<Version> {
        return (versions `for component` componentId).all()
    }

    override fun mergeWithExistingComponentInfo(partialModel: PartialComponentVersionModel) {
        var component = partialModel.component
        var partialVersion = partialModel.version
        component = getOrCreateComponent(component)
        var version = getExistingVersionForComponent(component, partialVersion)

        val newInterfaces: Set<Interface>
        val newUnitTestCases: Set<UnitTest>
        val newInteractionTestCases: Set<InteractionTest>

        partialVersion = Version.versionOf.modify(partialVersion) {EntityReference(component)}

        if (version == null) {
            versions save partialVersion
            newInterfaces = partialVersion.listeningTo + partialVersion.sendingTo
            newUnitTestCases =
                partialVersion.testedBy.flatMap { it.templateFor.filterIsInstance<UnitTest>() }.toSet()
            newInteractionTestCases =
                partialVersion.testedBy.flatMap { it.templateFor.filterIsInstance<InteractionTest>() }
                    .toSet()
        } else {
            val originalInterfaces = version.listeningTo + version.sendingTo
            val originalUnitTestCases =
                version.testedBy.flatMap { it.templateFor.filterIsInstance<UnitTest>() }.toSet()
            val originalInteractionTestCases =
                version.testedBy.flatMap { it.templateFor.filterIsInstance<InteractionTest>() }.toSet()
            version = version.mergeWithVersionInfo(partialVersion)
            versions save version
            val interfacesAfterUpdate = version.listeningTo + version.sendingTo
            newInterfaces = interfacesAfterUpdate - originalInterfaces
            val unitTestCasesAfterUpdate =
                version.testedBy.flatMap { it.templateFor.filterIsInstance<UnitTest>() }.toSet()
            val interactionTestCasesAfterUpdate =
                version.testedBy.flatMap { it.templateFor.filterIsInstance<InteractionTest>() }.toSet()
            newUnitTestCases = unitTestCasesAfterUpdate - originalUnitTestCases
            newInteractionTestCases = interactionTestCasesAfterUpdate - originalInteractionTestCases
        }
        newInterfaces.forEach {
            interfaceAddedEventPublisher.publishNewInterface(
                when (it) {
                    is IncomingInterface -> IncomingInterfaceAddedToVersionEvent(
                        it.id,
                        it.protocol.value
                    )

                    is OutgoingInterface -> OutgoingInterfaceAddedToVersionEvent(
                        it.id,
                        it.protocol.value
                    )
                }
            )
        }
        newUnitTestCases.forEach {
            testAddedEventPublisher.publishNewUnitTest(
                UnitTestAddedEvent(
                    EntityReference(it.id,it.version)
                )
            )
        }
        newInteractionTestCases.forEach {
            testAddedEventPublisher.publishNewInteractionTest(
                InteractionTestAddedEvent(
                    EntityReference(it.id,it.version)
                )
            )
        }
    }

    private fun getOrCreateComponent(component: Component): Component {
        val existingComponent = (components `find by id` component.id)
        if (existingComponent != null) {
            return existingComponent
        }
        return components add component
    }

    private fun getExistingVersionForComponent(component: Component, version: Version): Version? {
        if (components `find by id` component.id == null) {
            throw IllegalStateException("Component ${component.identifier} not found")
        }
        val componentVersions = versions `for component` component.id
        val existingVersion = componentVersions `find by id` version.id

        return existingVersion
    }

}