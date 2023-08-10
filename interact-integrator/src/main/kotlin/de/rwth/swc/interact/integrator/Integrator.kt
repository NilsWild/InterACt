package de.rwth.swc.interact.integrator

import de.rwth.swc.interact.domain.TestInvocationsDescriptor
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

    var interactionTestCases: List<TestInvocationsDescriptor> = listOf()
    private val props = Properties()
    private val logger = logger()
    var initialized = false

    init {
        props.load(this.javaClass.classLoader.getResourceAsStream("interact.properties"))
    }

    fun pullReplacements() {
        if (!initialized) {
            val client =
                IntegrationControllerApi(
                    props.getProperty("broker.url", "http://localhost:8080"),
                    vertx = Vertx.vertx()
                )

            client.getIntegrationsForComponent(
                componentInformationLoader.getComponentName().name,
                componentInformationLoader.getComponentVersion().version
            ).onSuccess {
                logger.info("Found ${it.size} integrations for component: $it")
                interactionTestCases = it
                initialized = true
            }.toCompletionStage().toCompletableFuture().join()
        }
    }
}