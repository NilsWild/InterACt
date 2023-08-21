package de.rwth.swc.interact.observer

import de.rwth.swc.interact.domain.*
import de.rwth.swc.interact.domain.serialization.SerializationConstants
import de.rwth.swc.interact.test.ComponentInformationLoader
import de.rwth.swc.interact.test.PropertiesBasedComponentInformationLoader
import de.rwth.swc.interact.utils.Logging
import de.rwth.swc.interact.utils.logger
import io.vertx.core.Vertx
import java.lang.reflect.Method
import java.util.*

/**
 * The TestObserver is used to observe the test execution and record the observed messages.
 */
object TestObserver : Logging {

    var componentInformationLoader: ComponentInformationLoader = PropertiesBasedComponentInformationLoader
    private var component: Component? = null
    private var currentTestCase: ConcreteTestCase? = null
    private var observations: MutableList<Component> = mutableListOf()

    private val props = Properties()
    private val log = logger()

    init {
        props.load(this.javaClass.classLoader.getResourceAsStream("interact.properties"))
    }

    fun startObservation(
        testClass: Class<*>,
        testMethod: Method,
        testParameters: List<TestCaseParameter?>,
        mode: TestMode
    ) {
        if (testParameters.isEmpty()) {
            throw java.lang.RuntimeException("A concrete test case name needs to be provided if no parameterized tests are used")
        }
        startObservation(
            testClass,
            AbstractTestCaseName(testMethod.name),
            ConcreteTestCaseName("(" + testParameters.joinToString(",") + ")"),
            testParameters,
            mode
        )
    }

    fun startObservation(
        testClass: Class<*>,
        abstractTestName: AbstractTestCaseName,
        concreteTestName: ConcreteTestCaseName,
        testParameters: List<TestCaseParameter?>,
        mode: TestMode
    ) {
        component = Component(
            componentInformationLoader.getComponentName(),
            componentInformationLoader.getComponentVersion()
        )
        component?.also { component ->
            observations.add(component)
            val abstractTestCase = AbstractTestCase(AbstractTestCaseSource(testClass.canonicalName), abstractTestName)
            component.abstractTestCases.add(abstractTestCase)
            val concreteTestCase = ConcreteTestCase(
                concreteTestName,
                mode,
                testParameters
            )
            abstractTestCase.concreteTestCases.add(concreteTestCase)
            currentTestCase = concreteTestCase
        }
    }

    fun recordMessage(observedMessage: Message) {
        currentTestCase?.observedMessages?.add(observedMessage) ?: throw RuntimeException("No test case started")

        when (observedMessage.messageType) {
            is MessageType.Sent -> (observedMessage as SentMessage).let { message ->
                log.info("SENT ${message.messageType}: ${message.value} to ${observedMessage.sentBy}")
            }
            is MessageType.Received -> (observedMessage as ReceivedMessage).let { message ->
                log.info("RECEIVED ${message.messageType}: ${message.value} from ${observedMessage.receivedBy}")
            }
        }
    }

    fun getObservations(): List<Component> {
        return observations
    }

    fun clear() {
        observations = mutableListOf()
        currentTestCase = null
        component = null
    }

    fun setTestResult(result: TestResult) {
        currentTestCase?.also { it.result = result } ?: throw RuntimeException("No test case started")
    }

    fun pushObservations() {
        val client =
            ObservationControllerApi(
                props.getProperty("broker.url", "http://localhost:8080"),
                SerializationConstants.mapper,
                Vertx.vertx()
            )

        client.storeObservations(observations).onSuccess {
            clear()
        }.toCompletionStage().toCompletableFuture().join()
    }

    fun dropObservation() {
        observations.removeLast()
    }

}