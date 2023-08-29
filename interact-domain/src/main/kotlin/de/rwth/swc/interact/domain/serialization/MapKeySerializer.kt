package de.rwth.swc.interact.domain.serialization

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import de.rwth.swc.interact.domain.ComponentId
import de.rwth.swc.interact.domain.ConcreteTestCaseId
import de.rwth.swc.interact.domain.MessageId
import de.rwth.swc.interact.domain.ReceivedMessage
import io.github.projectmapk.jackson.module.kogera.KotlinModule
import io.github.projectmapk.jackson.module.kogera.jacksonObjectMapper
import java.util.*

class MessageIdMapKeyDeserializer : KeyDeserializer() {
    override fun deserializeKey(key: String, ctx: DeserializationContext): MessageId {
        return MessageId(UUID.fromString(key))
    }
}

class ConcreteTestCaseIdMapKeyDeserializer : KeyDeserializer() {
    override fun deserializeKey(key: String, ctx: DeserializationContext): ConcreteTestCaseId {
        return ConcreteTestCaseId(UUID.fromString(key))
    }
}

class ComponentIdMapKeyDeserializer : KeyDeserializer() {
    override fun deserializeKey(key: String, ctx: DeserializationContext): ComponentId {
        return ComponentId(UUID.fromString(key))
    }
}

class ReceivedMessageMapKeyDeserializer : KeyDeserializer() {

    companion object {
        val objectMapper: ObjectMapper = jacksonObjectMapper().registerModules(KotlinModule.Builder().build())
    }

    override fun deserializeKey(key: String, ctx: DeserializationContext): ReceivedMessage {
        return objectMapper.readValue(key, ReceivedMessage::class.java)
    }
}

class ReceivedMessageMapKeySerializer : JsonSerializer<ReceivedMessage>() {
    companion object {
        val objectMapper: ObjectMapper = jacksonObjectMapper().registerModules(KotlinModule.Builder().build())
    }

    override fun serialize(message: ReceivedMessage, gen: JsonGenerator, sp: SerializerProvider) {
        gen.writeFieldName(objectMapper.writeValueAsString(message))
    }

}

object InteractModule : SimpleModule() {
    private fun readResolve(): Any = InteractModule

    init {
        this.addKeyDeserializer(MessageId::class.java, MessageIdMapKeyDeserializer())
        this.addKeyDeserializer(ConcreteTestCaseId::class.java, ConcreteTestCaseIdMapKeyDeserializer())
        this.addKeyDeserializer(ComponentId::class.java, ComponentIdMapKeyDeserializer())
        this.addKeyDeserializer(ReceivedMessage::class.java, ReceivedMessageMapKeyDeserializer())
        this.addKeySerializer(ReceivedMessage::class.java, ReceivedMessageMapKeySerializer())
    }
}