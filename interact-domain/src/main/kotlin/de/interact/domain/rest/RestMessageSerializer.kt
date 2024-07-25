package de.interact.domain.rest

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import de.interact.domain.serialization.SerializationConstants

class RestMessageSerializer: StdSerializer<RestMessage<*>>(RestMessage::class.java) {
    override fun serialize(value: RestMessage<*>, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        gen.writeStringField("path", value.path)
        gen.writeObjectField("parameters", value.parameters)
        gen.writeObjectField("headers", value.headers)
        gen.writeFieldName("body")
        val body = SerializationConstants.getMessageSerializer(value).writeBodyAsJsonString(value)
        gen.writeRawValue(body)
        if(value is RestMessage.Response) {
            gen.writeNumberField("statusCode", value.statusCode)
        }
        gen.writeEndObject()
    }
}