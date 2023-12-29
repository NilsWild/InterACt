package de.rwth.swc.interact.observer

import de.rwth.swc.interact.domain.*
import de.rwth.swc.interact.domain.serialization.SerializationConstants
import de.rwth.swc.interact.test.ComponentInformationLoader
import de.rwth.swc.interact.test.PropertiesBasedComponentInformationLoader
import de.rwth.swc.interact.utils.Logging
import de.rwth.swc.interact.utils.logger
import io.vertx.core.Vertx
import java.util.*

/**
 * The TestObserver is used to observe the test execution and record the observed messages.
 */
object TestObserver : Logging {

    var componentInformationLoader: ComponentInformationLoader = PropertiesBasedComponentInformationLoader
    var client: ObservationControllerApi
    var beforeStoringLatch: ObservationLatch? = null

    private var component: Component? = null
    private var currentTestCase: ConcreteTestCase? = null
    private var observations: MutableList<Component> = mutableListOf()

    private val props = Properties()
    private val log = logger()

    init {
        props.load(this.javaClass.classLoader.getResourceAsStream("interact.properties"))
        this.client = ObservationControllerApi(
            props.getProperty("broker.url", "http://localhost:8080"),
            SerializationConstants.mapper,
            Vertx.vertx()
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
        currentTestCase?.observedMessages?.add(observedMessage)
            ?: throw IllegalStateException("No test was registered for observation.")

        when (observedMessage.messageType) {
            is MessageType.Sent -> (observedMessage as SentMessage).let { message ->
                log.info("SENT ${message.messageType}: ${message.value} to ${observedMessage.sentBy}")
            }
            is MessageType.Received -> (observedMessage as ReceivedMessage).let { message ->
                log.info("RECEIVED ${message.messageType}: ${message.value} from ${observedMessage.receivedBy}")
            }
        }
    }

    fun clear() {
        observations = mutableListOf()
        currentTestCase = null
        component = null
    }

    fun setTestResult(result: TestResult) {
        currentTestCase?.also { it.result = result }
            ?: throw IllegalStateException("No test was registered for observation.")
    }

    fun pushObservations() {
        try {

            client.storeObservations(observations).onSuccess {
                clear()
            }.toCompletionStage().toCompletableFuture().join()
        } catch (e: Exception) {
            log.error("Error while pushing observations", e)
        }
    }

    fun dropObservation() {
        observations.removeLast()
    }

    fun dropLastMessage() {
        currentTestCase?.observedMessages?.removeLast()
    }
}

interface ObservationLatch {
    fun isReleased(): Boolean
}
