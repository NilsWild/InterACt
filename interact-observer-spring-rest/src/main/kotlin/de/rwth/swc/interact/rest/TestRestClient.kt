package de.rwth.swc.interact.rest

import org.springframework.http.HttpMethod
import org.springframework.web.reactive.function.client.WebClient

class TestRestClient(private val client: WebClient) {

    fun prepare(method: HttpMethod, uri: String, message: RestMessage<*>): WebClient.RequestHeadersSpec<*> {
        val request = client.method(method)
            .uri(uri, *message.pathVariables.toTypedArray())
            .headers { headers -> headers.addAll(message.headers) }
        message.body?.let { request.bodyValue(it) }
        return request
    }
}