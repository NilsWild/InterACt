package de.interact.junit.jupiter

import de.interact.domain.testobservation.ObservationToTwinMapper
import de.interact.integrator.Integrator
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import java.util.stream.Stream

class InterACtArgumentsProvider {

    fun provideArguments(context: ExtensionContext): Stream<Arguments> {
        val componentInfoLoader = ExtensionContextToTestInfoMapper.componentInformationLoader(context)
        val testCases = Integrator.interactionTestCases
        return testCases.filter {
            it.abstractTestId.value == ObservationToTwinMapper.abstractTestCaseId(
                ObservationToTwinMapper.versionId(
                    ObservationToTwinMapper.componentId(componentInfoLoader.getComponentName()),
                    componentInfoLoader.getComponentVersion()
                ),
                ExtensionContextToTestInfoMapper.abstractTestCaseSource(context),
                ExtensionContextToTestInfoMapper.abstractTestCaseName(context)
            ).value
        }.map { invocation ->
            Arguments.of(
                *invocation.parameters.map { it.value }.toTypedArray()
            )
        }.stream()
    }
}