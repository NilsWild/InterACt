package de.interact.integrator

import com.fasterxml.jackson.databind.ObjectMapper
import de.interact.domain.testexecution.TestInvocationDescriptor
import de.interact.utils.Logging
import de.interact.utils.logger
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.ext.web.client.WebClient

/**
 * The IntegrationControllerApi is used to get the interaction test case definitions that contain
 * the required manipulations for the interaction tests from the interact-controller.
 */
class IntegrationControllerApi(private val url: String, private val mapper: ObjectMapper, vertx: Vertx) : Logging {

    private val client: WebClient
    private val log = logger()

    init {
        client = WebClient.create(vertx)
    }

    fun getIntegrationsForComponent(name: String, version: String): Future<List<TestInvocationDescriptor>> {
        return client.getAbs("$url/api/integrations/$name/$version").timeout(30000).send()
            .map { response ->
                if (response.statusCode() != 200) {
                    log.error("Could not get integrations. Error: ${response.bodyAsString()}")
                    throw RuntimeException("Could not get integrations.")
                }
                val body = String(response.body().bytes, Charsets.UTF_8)
                if (body.isEmpty()) {
                    log.info("No integrations found for component $name:$version")
                }
                mapper.readerForListOf(TestInvocationDescriptor::class.java)
                    .readValue<List<TestInvocationDescriptor>?>(body).also {
                        log.info("Found ${it?.size ?: 0} integrations for component $name:$version")
                    }
            }
    }
}