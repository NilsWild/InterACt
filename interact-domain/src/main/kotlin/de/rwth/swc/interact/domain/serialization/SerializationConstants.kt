package de.rwth.swc.interact.domain.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.projectmapk.jackson.module.kogera.KotlinFeature
import io.github.projectmapk.jackson.module.kogera.KotlinModule
import io.github.projectmapk.jackson.module.kogera.jacksonObjectMapper

object SerializationConstants {
    val mapper: ObjectMapper = jacksonObjectMapper().registerModules(
        InteractModule,
        KotlinModule.Builder().configure(
            KotlinFeature.SingletonSupport, true
        ).build()
    )
}