package de.rwth.swc.interact.observer

import de.rwth.swc.interact.controller.client.api.ObservationControllerApi
import de.rwth.swc.interact.observer.domain.*
import de.rwth.swc.interact.test.ComponentInformationLoader
import de.rwth.swc.interact.test.ExITConfiguration
import de.rwth.swc.interact.test.PropertiesBasedComponentInformationLoader
import okhttp3.OkHttpClient
import java.lang.reflect.Method
import java.util.*

object TestObserver {

    var componentInformationLoader: ComponentInformationLoader = PropertiesBasedComponentInformationLoader()
    private var observedComponentInfo: ComponentInfo? = null
    private var currentTestCase: ConcreteTestCaseInfo? = null
    private var observations: MutableList<ComponentInfo> = mutableListOf()

    private val props = Properties()

    init {
        props.load(this.javaClass.classLoader.getResourceAsStream("interact.properties"))
    }

    fun startObservation(
        testClass: Class<*>,
        testMethod: Method,
        testParameters: List<String>
    ) {
        if (testParameters.isEmpty()) {
            throw java.lang.RuntimeException("A concrete test case name needs to be provided if no parameterized tests are used")
        }
        startObservation(
            testClass,
            testMethod.name,
            "(" + testParameters.joinToString(",") + ")",
            testParameters
        )
    }

    fun startObservation(
        testClass: Class<*>,
        abstractTestName: String,
        concreteTestName: String,
        testParameters: List<String>
    ) {
        val component = ComponentInfo(
            componentInformationLoader.getComponentName(),
            componentInformationLoader.getComponentVersion()
        )

        observedComponentInfo = component
        observations.add(observedComponentInfo!!)

        val abstractTestCaseInfo = AbstractTestCaseInfo(testClass.canonicalName, abstractTestName)

        observedComponentInfo!!.abstractTestCaseInfo = abstractTestCaseInfo

        val concreteTestCaseInfo = ConcreteTestCaseInfo(
            concreteTestName,
            ExITConfiguration.mode,
            testParameters
        )
        observedComponentInfo!!.abstractTestCaseInfo!!.concreteTestCaseInfo = concreteTestCaseInfo
        currentTestCase = concreteTestCaseInfo
    }

    fun setTestedInteractionExpectation(id: UUID) {
        currentTestCase?.interactionExpectationId = id
    }

    fun recordMessage(observedMessage: ObservedMessage) {
        currentTestCase!!.observedMessages.add(observedMessage)
    }

    fun getObservations(): List<ComponentInfo> {
        return observations
    }

    fun clear() {
        observations = mutableListOf()
        currentTestCase = null
        observedComponentInfo = null
    }

    fun setTestResult(result: ObservedTestResult) {
        currentTestCase?.result = result
    }

    fun pushObservations() {
        val client = ObservationControllerApi(props.getProperty("broker.url", "http://localhost:8080"), OkHttpClient())
        try {
            client.storeObservations(observations)
            clear()
        } catch (_: java.lang.Exception) {

        }
    }

    fun dropObservation() {
        observations.removeLast()
    }

}