package de.rwth.swc.interact.junit.jupiter.systemexpectations

import com.fasterxml.jackson.databind.ObjectMapper
import de.rwth.swc.interact.domain.Component
import de.rwth.swc.interact.utils.Logging
import de.rwth.swc.interact.utils.logger
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient

class SystemPropertyExpectationControllerApi(
    private val url: String,
    private val mapper: ObjectMapper,
    vertx: Vertx
) : Logging {

    private val client: WebClient
    private val log = logger()

    init {
        client = WebClient.create(vertx)
    }

    fun storeSystemPropertyExpectation(expectation: Component): Future<HttpResponse<Buffer>> {
        val body = mapper.writeValueAsBytes(expectation)
        log.info("Storing ${expectation.systemPropertyExpectations.size} expectations")
        return client.postAbs("$url/api/system-property-expectations")
            .putHeader("Content-Type", "application/json")
            .sendBuffer(Buffer.buffer(body))
            .onSuccess {
                if (it.statusCode() != 200) {
                    log.error("Could not store expectations. Error: ${it.bodyAsString()}")
                } else {
                    log.info("Stored ${expectation.systemPropertyExpectations.size} observations")
                }
            }.onFailure {
                log.error("Could not store expectations", it)
            }
    }
}