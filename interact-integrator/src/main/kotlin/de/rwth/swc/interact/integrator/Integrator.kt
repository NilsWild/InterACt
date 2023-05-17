package de.rwth.swc.interact.integrator

import de.rwth.swc.interact.controller.client.api.IntegrationControllerApi
import de.rwth.swc.interact.integrator.domain.InteractionTestCases
import de.rwth.swc.interact.integrator.domain.MessageData
import de.rwth.swc.interact.integrator.domain.TestCaseReference
import de.rwth.swc.interact.test.ComponentInformationLoader
import de.rwth.swc.interact.test.PropertiesBasedComponentInformationLoader
import okhttp3.OkHttpClient
import java.util.*

object Integrator {
    var componentInformationLoader: ComponentInformationLoader = PropertiesBasedComponentInformationLoader()

    private var interactionTestCases: List<InteractionTestCases> = listOf()
    private var testCaseReference: TestCaseReference? = null
    private var iteration: Int = 0
    private val props = Properties()

    init {
        props.load(this.javaClass.classLoader.getResourceAsStream("interact.properties"))
    }

    fun pullReplacements() {
        val client = IntegrationControllerApi(props.getProperty("broker.url", "http://localhost:8080"), OkHttpClient())
        try {
            interactionTestCases = client.getIntegrationsForComponent(
                componentInformationLoader.getComponentName(),
                componentInformationLoader.getComponentVersion()
            )
        } catch (_: java.lang.Exception) {

        }
    }

    fun startTestCase(testCaseReference: TestCaseReference, iteration: Int = 0): UUID? {
        this.testCaseReference = testCaseReference
        this.iteration = iteration
        return interactionTestCases.firstOrNull { it.testCaseReference == testCaseReference }?.interactionExpectation
    }

    fun getReplacement(messageData: MessageData): MessageData? {
        return testCaseReference?.let {
            return interactionTestCases.firstOrNull { it.testCaseReference == testCaseReference }?.testCases?.get(
                iteration
            )?.replacements?.firstOrNull { it.original == messageData }?.replacement
        } ?: throw RuntimeException("Can not get replacements for messages before test is started!")
    }

    fun doesReplacementExist(): Boolean {
        return testCaseReference?.let {
            return !interactionTestCases.firstOrNull { it.testCaseReference == testCaseReference }?.testCases.isNullOrEmpty()
        } ?: throw RuntimeException("Can not get replacements for messages before test is started!")
    }
}