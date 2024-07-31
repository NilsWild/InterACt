package de.interact.domain.serialization

import com.fasterxml.jackson.databind.JavaType
import de.interact.domain.expectations.validation.plan.InteractionGraph
import de.interact.domain.rest.RestMessage
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SerializationTest {

    init {
        SerializationConstants.registerMessageSerializer(RestMessageBodySerializer(SerializationConstants.mapper))
        SerializationConstants.registerMessageDeserializer(RestMessageBodyDeserializer(SerializationConstants.mapper))
    }

    @Test
    fun serialize() {
        val req = RestMessage.Request(
            path = "/path/1",
            parameters = mapOf("param" to "value"),
            headers = mapOf("header" to "value"),
            body = "{\"body\":\"value\"}"
        )
        val serialized = SerializationConstants.mapper.writeValueAsString(req)
        val deserialized: RestMessage.Request<Map<String, String>> = SerializationConstants.mapper.readValue<RestMessage.Request<Map<String, String>>>(
            serialized,
            SerializationConstants.mapper.typeFactory.constructParametricType(
                RestMessage.Request::class.java,
                SerializationConstants.mapper.typeFactory.constructMapType(
                    HashMap::class.java,
                    String::class.java,
                    String::class.java
                )
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
        val serialized = SerializationConstants.mapper.writeValueAsString(
            req
        )
        val deserialized: RestMessage.Request<Map<String, String>> = SerializationConstants.mapper.readValue(
            serialized,
            SerializationConstants.mapper.typeFactory.constructParametricType(
                RestMessage.Request::class.java,
                Map::class.java
            )
        )
        deserialized shouldBe req
    }

    private fun resolveTypeReference(type: Type): JavaType {
        return if (type is ParameterizedType) {
            val genericTypes =
                type.actualTypeArguments.map { resolveTypeReference(it) }.toTypedArray()
            SerializationConstants.mapper.typeFactory.constructParametricType(
                type.rawType as Class<*>,
                *genericTypes
            )
        } else {
            SerializationConstants.mapper.typeFactory.constructType(type)
        }
    }

    @Test
    fun test() {
        val i = InteractionGraph()
        val copy = i.copy()
        i shouldBe copy
    }
}