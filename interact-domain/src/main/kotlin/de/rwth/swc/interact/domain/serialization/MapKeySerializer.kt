package de.rwth.swc.interact.domain.serialization

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.KeyDeserializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import de.rwth.swc.interact.domain.ComponentId
import de.rwth.swc.interact.domain.ConcreteTestCaseId
import de.rwth.swc.interact.domain.MessageId
import de.rwth.swc.interact.domain.ReceivedMessage
import io.github.projectmapk.jackson.module.kogera.jacksonObjectMapper
import java.util.*

class MessageIdMapKeyDeserializer: KeyDeserializer() {
    override fun deserializeKey(key: String, ctx: DeserializationContext): MessageId {
        return MessageId(UUID.fromString(key))
    }
}

class ConcreteTestCaseIdMaoKeyDeserializer: KeyDeserializer() {
    override fun deserializeKey(key: String, ctx: DeserializationContext): ConcreteTestCaseId {
        return ConcreteTestCaseId(UUID.fromString(key))
    }
}

class ComponentIdMapKeyDeserializer: KeyDeserializer() {
    override fun deserializeKey(key: String, ctx: DeserializationContext): ComponentId {
        return ComponentId(UUID.fromString(key))
    }
}

class ReceivedMessageMapKeyDeserializer: KeyDeserializer() {

    companion object {
        val objectMapper = jacksonObjectMapper()
    }

    override fun deserializeKey(key: String, ctx: DeserializationContext): ReceivedMessage {
        return objectMapper.readValue(key, ReceivedMessage::class.java)
    }
}

class ReceivedMessageMapKeySerializer: JsonSerializer<ReceivedMessage>() {
    companion object {
        val objectMapper = jacksonObjectMapper()
    }
    override fun serialize(message: ReceivedMessage, gen: JsonGenerator, sp: SerializerProvider) {
        gen.writeFieldName(objectMapper.writeValueAsString(message))
    }

}

object InteractModule: SimpleModule() {
    init {
        this.addKeyDeserializer(MessageId::class.java, MessageIdMapKeyDeserializer())
        this.addKeyDeserializer(ConcreteTestCaseId::class.java, ConcreteTestCaseIdMaoKeyDeserializer())
        this.addKeyDeserializer(ComponentId::class.java, ComponentIdMapKeyDeserializer())
        this.addKeyDeserializer(ReceivedMessage::class.java, ReceivedMessageMapKeyDeserializer())
        this.addKeySerializer(ReceivedMessage::class.java, ReceivedMessageMapKeySerializer())
    }
}