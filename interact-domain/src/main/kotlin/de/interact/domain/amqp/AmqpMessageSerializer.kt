package de.interact.domain.amqp

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import de.interact.domain.serialization.SerializationConstants

class AmqpMessageSerializer: StdSerializer<AmqpMessage<*>>(AmqpMessage::class.java) {
    override fun serialize(value: AmqpMessage<*>, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        gen.writeObjectField("headers", value.headers)
        val body = SerializationConstants.getMessageSerializer(value).writeBodyAsString(value)
        gen.writeStringField("body", body)
    }
}