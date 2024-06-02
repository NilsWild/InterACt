package de.interact.domain.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.github.projectmapk.jackson.module.kogera.KotlinFeature
import io.github.projectmapk.jackson.module.kogera.KotlinModule
import io.github.projectmapk.jackson.module.kogera.jacksonObjectMapper

object SerializationConstants {
    var mapper: ObjectMapper = jacksonObjectMapper().registerModules(
        InteractModule,
        KotlinModule.Builder().configure(
            KotlinFeature.SingletonSupport, true
        ).build(),
        JavaTimeModule()
    )
    var messageMapper: ObjectMapper = jacksonObjectMapper().registerModules(
        InteractModule,
        KotlinModule.Builder().configure(
            KotlinFeature.SingletonSupport, true
        ).build(),
        JavaTimeModule()
    )
}