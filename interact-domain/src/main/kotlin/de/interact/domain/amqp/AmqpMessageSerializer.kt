package de.interact.domain.amqp

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import de.interact.domain.serialization.SerializationConstants

class AmqpMessageSerializer: StdSerializer<AmqpMessage<*>>(AmqpMessage::class.java) {
    override fun serialize(value: AmqpMessage<*>, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        gen.writeObjectField("headers", value.headers)
        gen.writeFieldName("body")
        val body = SerializationConstants.getMessageSerializer(value).writeBodyAsJsonString(value)
        gen.writeRawValue(body)
        gen.writeEndObject()
    }
}