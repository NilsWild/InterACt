package de.interact.integrator

import de.interact.domain.serialization.SerializationConstants
import de.interact.domain.testexecution.TestInvocationDescriptor
import de.interact.test.ComponentInformationLoader
import de.interact.test.PropertiesBasedComponentInformationLoader
import de.interact.utils.Logging
import de.interact.utils.logger
import io.vertx.core.Vertx
import java.util.*

/**
 * The Integrator is used to manipulate the test cases to drive the interaction tests.
 */
object Integrator : Logging {
    var componentInformationLoader: ComponentInformationLoader = PropertiesBasedComponentInformationLoader

    var interactionTestCases: List<TestInvocationDescriptor> = listOf()
    private val props = Properties()
    private val logger = logger()
    var initialized = false

    init {
        props.load(this.javaClass.classLoader.getResourceAsStream("interact.properties"))
    }

    fun pullReplacements() {
        if (!initialized) {
            try {
                val client =
                    IntegrationControllerApi(
                        props.getProperty("broker.url", "http://localhost:8080"),
                        SerializationConstants.mapper,
                        Vertx.vertx()
                    )

                client.getIntegrationsForComponent(
                    componentInformationLoader.getComponentName().value,
                    componentInformationLoader.getComponentVersion().value
                ).onSuccess {
                    logger.info("Found ${it.size} integrations for component: $it")
                    interactionTestCases = it
                    initialized = true
                }.toCompletionStage().toCompletableFuture().join()
            } catch (e: Exception) {
                logger.error("Could not pull replacements from broker!", e)
                initialized = true
            }
        }
    }
}