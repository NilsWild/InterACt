package de.interact.domain.rest

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize

sealed interface RestMessage<T> {
    val path: String
    val parameters: Map<String, String>
    val headers: Map<String, String>
    @get:JsonSerialize(using = BodySerializer::class)
    val body: T?

    data class Request<T>(
        override val path: String,
        override val parameters: Map<String, String>,
        override val headers: Map<String, String>,
        override val body: T?
    ) : RestMessage<T>

    data class Response<T>(
        override val path: String,
        override val parameters: Map<String, String>,
        override val headers: Map<String, String>,
        override val body: T?,
        val statusCode: Int
    ) : RestMessage<T>
}

class BodySerializer : JsonSerializer<Any>() {
    override fun serialize(value: Any, gen: JsonGenerator, serializers: SerializerProvider) {
        if(value is String) {
            gen.writeString(value)
        }else {
            gen.writeObject(value)
        }
    }
}