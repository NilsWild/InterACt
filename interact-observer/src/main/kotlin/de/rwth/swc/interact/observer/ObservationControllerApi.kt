package de.rwth.swc.interact.observer

import com.fasterxml.jackson.databind.ObjectMapper
import de.rwth.swc.interact.domain.Component
import de.rwth.swc.interact.utils.Logging
import de.rwth.swc.interact.utils.logger
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient

/**
 * The ObservationControllerApi is used to send the observations to the interact-controller.
 */
class ObservationControllerApi(private val url: String, private val mapper: ObjectMapper, vertx: Vertx) : Logging {

    private val client: WebClient
    private val log = logger()

    init {
        client = WebClient.create(vertx)
    }

    fun storeObservations(observations: List<Component>): Future<HttpResponse<Buffer>> {
        val body = mapper.writeValueAsBytes(observations)
        log.info("Storing ${observations.size} observations")
        return client.postAbs("$url/api/observations")
            .putHeader("Content-Type", "application/json")
            .sendBuffer(Buffer.buffer(body))
            .onSuccess {
                if (it.statusCode() != 200) {
                    log.error("Could not store observations. Error: ${it.bodyAsString()}")
                } else {
                    log.info("Stored ${observations.size} observations")
                }
            }.onFailure {
                log.error("Could not store observations", it)
            }
    }
}
