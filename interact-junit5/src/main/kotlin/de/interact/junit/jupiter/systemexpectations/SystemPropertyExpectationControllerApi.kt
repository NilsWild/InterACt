package de.interact.junit.jupiter.systemexpectations

import com.fasterxml.jackson.databind.ObjectMapper
import de.interact.domain.expectations.execution.result.ExpectationsExecutionResult
import de.interact.domain.specification.spi.ExpectationsPublisher
import de.interact.utils.Logging
import de.interact.utils.logger
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient

class SystemPropertyExpectationControllerApi(
    private val url: String,
    private val mapper: ObjectMapper,
    vertx: Vertx
) : ExpectationsPublisher, Logging {

    private val client: WebClient
    private val log = logger()

    init {
        client = WebClient.create(vertx)
    }

    override fun publish(result: ExpectationsExecutionResult): Boolean {
        storeSystemPropertyExpectation(result).toCompletionStage().toCompletableFuture().join()
        return true
    }

    fun storeSystemPropertyExpectation(result: ExpectationsExecutionResult): Future<HttpResponse<Buffer>> {
        val body = mapper.writeValueAsBytes(result)
        return client.postAbs("$url/api/system-property-expectations")
            .putHeader("Content-Type", "application/json")
            .sendBuffer(Buffer.buffer(body))
            .onSuccess {
                if (it.statusCode() != 200) {
                    log.error("Could not store expectations. Error: ${it.bodyAsString()}")
                } else {
                    log.info("Stored expectations")
                }
            }.onFailure {
                log.error("Could not store expectations", it)
            }
    }
}