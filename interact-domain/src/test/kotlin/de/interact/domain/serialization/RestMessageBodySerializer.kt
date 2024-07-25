package de.interact.domain.serialization

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import de.interact.domain.rest.RestMessage
import de.interact.domain.shared.Message

class RestMessageBodySerializer(private val mapper: ObjectMapper, override val order: Int = Integer.MIN_VALUE): MessageSerializer {

    override fun canHandle(message: Message<*>): Boolean {
        return message is RestMessage
    }

    override fun writeBodyAsJsonString(value: Message<*>): String {
        return if(value.body is String) {
            value.body as String
        }else {
            mapper.writeValueAsString(value.body)
        }
    }

}