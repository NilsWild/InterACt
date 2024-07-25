package de.interact.domain.serialization

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import de.interact.domain.rest.RestMessage
import de.interact.domain.shared.Message

class RestMessageBodyDeserializer(private val mapper: ObjectMapper, override val order: Int = Integer.MIN_VALUE): MessageDeserializer {

    override fun readBody(value: Message<String>, bodyType: JavaType): Any {
        return mapper.readValue(value.body, bodyType)
    }

    override fun canHandle(message: Message<String>): Boolean {
        return message is RestMessage
    }

}