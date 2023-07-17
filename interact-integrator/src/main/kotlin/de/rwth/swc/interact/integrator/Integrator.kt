package de.rwth.swc.interact.integrator

import de.rwth.swc.interact.domain.*
import de.rwth.swc.interact.test.ComponentInformationLoader
import de.rwth.swc.interact.test.PropertiesBasedComponentInformationLoader
import de.rwth.swc.interact.utils.Logging
import de.rwth.swc.interact.utils.logger
import io.vertx.core.Vertx
import java.util.*

/**
 * The Integrator is used to manipulate the test cases to drive the interaction tests.
 */
object Integrator: Logging {
    var componentInformationLoader: ComponentInformationLoader = PropertiesBasedComponentInformationLoader

    private var interactionTestCases: List<TestInvocationsDescriptor> = listOf()
    private var abstractTestCase: AbstractTestCase? = null
    private var iteration: Int = 0
    private val props = Properties()
    private val logger = logger()

    init {
        props.load(this.javaClass.classLoader.getResourceAsStream("interact.properties"))
    }

    fun pullReplacements() {
        val client =
            IntegrationControllerApi(props.getProperty("broker.url", "http://localhost:8080"), vertx = Vertx.vertx())

        client.getIntegrationsForComponent(
            componentInformationLoader.getComponentName().name,
            componentInformationLoader.getComponentVersion().version
        ).onSuccess {
            logger.info("Found ${it.size} integrations for component: $it")
            interactionTestCases = it
        }.toCompletionStage().toCompletableFuture().join()
    }

    fun startTestCase(abstractTestCase: AbstractTestCase, iteration: Int = 0): InteractionExpectationId? {
        this.abstractTestCase = abstractTestCase
        this.iteration = iteration
        return interactionTestCases.firstOrNull { it.abstractTestCase == abstractTestCase }?.testInvocations?.get(iteration)?.interactionExpectationId
    }

    fun getReplacement(messageData: ReceivedMessage): SentMessage? {
        return abstractTestCase?.let {
            return interactionTestCases.firstOrNull { it.abstractTestCase == abstractTestCase }?.testInvocations?.get(
                iteration
            )?.replacements?.get(messageData)
        } ?: throw RuntimeException("Can not get replacements for messages before test is started!")
    }

    fun doesReplacementExist(): Boolean {
        return abstractTestCase?.let {
            return !interactionTestCases.firstOrNull { it.abstractTestCase == abstractTestCase }?.testInvocations.isNullOrEmpty()
        } ?: throw RuntimeException("Can not get replacements for messages before test is started!")
    }
}