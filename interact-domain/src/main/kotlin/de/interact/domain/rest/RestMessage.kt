package de.interact.domain.rest

import arrow.core.raise.catch
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer
import com.fasterxml.jackson.databind.ser.ContextualSerializer
import de.interact.domain.serialization.SerializationConstants
import io.github.projectmapk.jackson.module.kogera.readValue

sealed interface RestMessage<T> {
    val path: String
    val parameters: Map<String, String>
    val headers: Map<String, String>
    @get:JsonDeserialize(using = BodyDeserializer::class)
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
            if(value.isEmpty()) {
                gen.writeRawValue("")
            } else {
                catch({
                    SerializationConstants.mapper.readTree(value)
                    gen.writeRawValue(value)
                }) {
                    gen.writeString(value)
                }
            }
        }else {
            gen.writeRawValue(SerializationConstants.messageMapper.writeValueAsString(value))
        }
    }
}

class BodyDeserializer : JsonDeserializer<Any>(), ContextualDeserializer {

    private var valueType: JavaType? = null

    override fun createContextual(ctx: DeserializationContext, property: BeanProperty?): JsonDeserializer<*> {
        val bodyType = property?.type ?: ctx.contextualType
        return BodyDeserializer().apply { valueType = bodyType }
    }

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Any {
        return SerializationConstants.messageMapper.readValue(p, valueType)
    }

}