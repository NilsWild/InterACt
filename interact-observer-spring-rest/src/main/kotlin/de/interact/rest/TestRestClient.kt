package de.interact.rest

import de.interact.utils.MultiMap
import org.springframework.http.HttpHeaders
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

fun HttpHeaders.addAll(headers: MultiMap<String, String>) {
    headers.keys.forEach { key ->
        this.addAll(key, headers[key].toMutableList())
    }
}

fun HttpHeaders.toMultiMap(): MultiMap<String, String> {
    val multiMap = MultiMap<String, String>()
    this.forEach { key, values ->
        multiMap.putAll(key, values)
    }
    return multiMap
}

