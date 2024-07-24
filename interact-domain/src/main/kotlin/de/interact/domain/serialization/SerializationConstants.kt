package de.interact.domain.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
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

    var messageMappers: SortedSet<MessageMapper> = sortedSetOf()
        private set

    fun registerMessageMapper(messageMapper: MessageMapper) {
        messageMappers += messageMapper
    }

    fun getMessageMapper(type: Class<*>): MessageMapper {
        return messageMappers.first { it.canHandle(type) }
            ?: throw IllegalArgumentException("No message mapper found for type $type")
    }
}