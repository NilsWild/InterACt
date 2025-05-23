package de.interact.rest

import de.interact.domain.rest.RestMessage
import de.interact.utils.MultiMap
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.web.reactive.function.client.WebClient

class TestRestClient(private val client: WebClient) {

    fun prepare(method: HttpMethod, message: RestMessage<*>): WebClient.RequestHeadersSpec<*> {
        val request = client.method(method)
            .uri(message.path)
            .headers { headers -> message.headers.forEach{ headers.addAll(it.key, it.value.split(","))} }
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

