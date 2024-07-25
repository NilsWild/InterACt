package de.interact.domain.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import de.interact.domain.shared.Message
import io.github.projectmapk.jackson.module.kogera.KotlinFeature
import io.github.projectmapk.jackson.module.kogera.KotlinModule
import io.github.projectmapk.jackson.module.kogera.jacksonObjectMapper
import java.util.*

object SerializationConstants {
    var mapper: ObjectMapper = jacksonObjectMapper().registerModules(
        InteractModule,
        KotlinModule.Builder().configure(
            KotlinFeature.SingletonSupport, true
        ).build(),
        JavaTimeModule()
    )

    private var messageSerializers: SortedSet<MessageSerializer> = sortedSetOf()
    private var messageDeserializers: SortedSet<MessageDeserializer> = sortedSetOf()

    fun registerMessageSerializer(messageSerializer: MessageSerializer) {
        messageSerializers += messageSerializer
    }

    fun registerMessageDeserializer(messageSerializer: MessageDeserializer) {
        messageDeserializers += messageSerializer
    }

    fun getMessageSerializer(message: Message<*>): MessageSerializer {
        return messageSerializers.first { it.canHandle(message) }
            ?: throw IllegalArgumentException("No message serializer found for message $message")
    }

    fun getMessageDeserializer(message: Message<String>): MessageDeserializer {
        return messageDeserializers.first { it.canHandle(message) }
            ?: throw IllegalArgumentException("No message deserializer found for message $message")
    }
}