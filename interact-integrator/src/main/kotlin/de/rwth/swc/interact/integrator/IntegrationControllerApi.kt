package de.rwth.swc.interact.integrator

import de.rwth.swc.interact.domain.TestInvocationsDescriptor
import de.rwth.swc.interact.domain.serialization.InteractModule
import de.rwth.swc.interact.utils.Logging
import de.rwth.swc.interact.utils.logger
import io.github.projectmapk.jackson.module.kogera.jacksonObjectMapper
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.ext.web.client.WebClient

/**
 * The IntegrationControllerApi is used to get the interaction test case definitions that contain
 * the required manipulations for the interaction tests from the interact-controller.
 */
class IntegrationControllerApi(private val url: String, vertx: Vertx): Logging {

    private val client: WebClient
    private val objectMapper = jacksonObjectMapper()
    private val log = logger()

    init {
        client = WebClient.create(vertx)
        objectMapper.registerModule(InteractModule)
    }

    fun getIntegrationsForComponent(name: String, version: String): Future<List<TestInvocationsDescriptor>> {
        return client.getAbs("$url/api/integrations/$name/$version").send()
            .map { response ->
                if (response.statusCode() != 200) {
                    log.error("Could not get integrations. Error: ${response.bodyAsString()}")
                    throw RuntimeException("Could not get integrations.")
                }
                val body = String(response.body().bytes, Charsets.UTF_8)
                if(body.isEmpty()) {
                    log.info("No integrations found for component $name:$version")
                }
                objectMapper.readerForListOf(TestInvocationsDescriptor::class.java).readValue<List<TestInvocationsDescriptor>?>(body).also {
                    log.info("Found ${it?.size ?: 0} integrations for component $name:$version")
                }
            }
    }
}