package de.interact.domain.testobservation

import com.fasterxml.uuid.Generators
import de.interact.domain.shared.AbstractTestId
import de.interact.domain.shared.ComponentId
import de.interact.domain.shared.VersionId
import de.interact.domain.testobservation.model.*
import de.interact.domain.testtwin.abstracttest.AbstractTestCaseIdentifier

object ObservationToTwinMapper {

    fun componentId(component: ObservedComponent): ComponentId {
        return componentId(component.name)
    }

    fun componentId(name: ComponentName): ComponentId {
        return ComponentId(Generators.nameBasedGenerator().generate("Component:${name}"))
    }

    fun versionId(componentId: ComponentId, version: ComponentVersion): VersionId {
        return VersionId(Generators.nameBasedGenerator().generate("$componentId:Version:$version"))
    }

    fun abstractTestCaseId(versionId: VersionId, abstractTestCase: AbstractTestCase): AbstractTestId {
        return abstractTestCaseId(versionId, abstractTestCase.source, abstractTestCase.name)
    }

    fun abstractTestCaseId(
        versionId: VersionId,
        abstractTestCaseSource: AbstractTestCaseSource,
        abstractTestCaseName: AbstractTestCaseName
    ): AbstractTestId {
        val abstractTestCaseIdentifier = AbstractTestCaseIdentifier("$abstractTestCaseSource:$abstractTestCaseName")
        return AbstractTestId(
            Generators.nameBasedGenerator().generate("$versionId:AbstractTestCase:$abstractTestCaseIdentifier")
        )
    }

    fun abstractTestCaseIdentifier(
        abstractTestCaseSource: AbstractTestCaseSource,
        abstractTestCaseName: AbstractTestCaseName
    ): AbstractTestCaseIdentifier {
        return AbstractTestCaseIdentifier("$abstractTestCaseSource:$abstractTestCaseName")
    }

    fun abstractTestCaseIdentifier(abstractTestCase: AbstractTestCase): AbstractTestCaseIdentifier {
        return abstractTestCaseIdentifier(abstractTestCase.source, abstractTestCase.name)
    }
}