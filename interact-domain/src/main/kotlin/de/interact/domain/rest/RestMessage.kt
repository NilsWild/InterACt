package de.interact.domain.rest

import arrow.optics.optics
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import de.interact.domain.shared.Message

@JsonSerialize(using = RestMessageSerializer::class)
@JsonDeserialize(using = RestMessageDeserializer::class)
@optics
sealed interface RestMessage<T>: Message<T> {
    companion object {}

    val path: String
    val parameters: Map<String, String>
    val headers: Map<String, String>
    override val body: T?

    @optics
    data class Request<T>(
        override val path: String,
        override val parameters: Map<String, String>,
        override val headers: Map<String, String>,
        override val body: T?
    ) : RestMessage<T> {companion object{}}

    @optics
    data class Response<T>(
        override val path: String,
        override val parameters: Map<String, String>,
        override val headers: Map<String, String>,
        override val body: T?,
        val statusCode: Int
    ) : RestMessage<T>{companion object{}}
}