package de.interact.rest.observer

import de.interact.domain.rest.RestMessage
import de.interact.domain.serialization.SerializationConstants
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SerializationTest {

    @Test
    fun serialize() {
        val req = RestMessage.Request(
            path = "/path/1",
            parameters = mapOf("param" to "value"),
            headers = mapOf("header" to "value"),
            body = "{\"body\":\"value\"}"
        )
        val serialized = SerializationConstants.messageMapper.writeValueAsString(
            req
        )
        val deserialized: RestMessage.Request<Map<String, String>> = SerializationConstants.messageMapper.readValue(
            serialized,
            SerializationConstants.messageMapper.typeFactory.constructParametricType(
                RestMessage.Request::class.java,
                Map::class.java
            )
        )
        deserialized.body shouldBe mapOf("body" to "value")
    }

    @Test
    fun serialize2() {
        val req = RestMessage.Request(
            path = "/path/1",
            parameters = mapOf("param" to "value"),
            headers = mapOf("header" to "value"),
            body = mapOf("body" to "value")
        )
        val serialized = SerializationConstants.messageMapper.writeValueAsString(
            req
        )
        val deserialized: RestMessage.Request<Map<String, String>> = SerializationConstants.messageMapper.readValue(
            serialized,
            SerializationConstants.messageMapper.typeFactory.constructParametricType(
                RestMessage.Request::class.java,
                Map::class.java
            )
        )
        deserialized shouldBe req
    }
}