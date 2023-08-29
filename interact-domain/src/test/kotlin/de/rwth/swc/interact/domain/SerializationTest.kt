package de.rwth.swc.interact.domain

import de.rwth.swc.interact.domain.serialization.SerializationConstants
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*


internal class SerializationTest {

    @Test
    fun `can serialize and deserialize map with MessageId as key`() {
        val mapper = SerializationConstants.mapper
        val map = mapOf(
            MessageId(UUID.fromString("0a72761f-32bd-44ab-bd20-d2d0898a7514")) to "value"
        )
        val serialized = mapper.writeValueAsString(map)
        val deserialized = mapper
            .readerFor(mapper.typeFactory.constructMapType(Map::class.java, MessageId::class.java, String::class.java))
            .readValue<Map<MessageId, String>>(serialized)
        assertThat(map).isEqualTo(deserialized)
    }

    @Test
    fun `can serialize and deserialize map with ConcreteTestCaseId as key`() {
        val mapper = SerializationConstants.mapper
        val map = mapOf(
            ConcreteTestCaseId(UUID.fromString("0a72761f-32bd-44ab-bd20-d2d0898a7514")) to "value"
        )
        val serialized = mapper.writeValueAsString(map)
        val deserialized = mapper
            .readerFor(
                mapper.typeFactory.constructMapType(
                    Map::class.java,
                    ConcreteTestCaseId::class.java,
                    String::class.java
                )
            )
            .readValue<Map<ConcreteTestCaseId, String>>(serialized)
        assertThat(map).isEqualTo(deserialized)
    }

    @Test
    fun `can serialize and deserialize map with ComponentId as key`() {
        val mapper = SerializationConstants.mapper
        val map = mapOf(
            ComponentId(UUID.fromString("0a72761f-32bd-44ab-bd20-d2d0898a7514")) to "value"
        )
        val serialized = mapper.writeValueAsString(map)
        val deserialized = mapper
            .readerFor(
                mapper.typeFactory.constructMapType(
                    Map::class.java,
                    ComponentId::class.java,
                    String::class.java
                )
            )
            .readValue<Map<ComponentId, String>>(serialized)
        assertThat(map).isEqualTo(deserialized)
    }

    @Test
    fun `can serialize and deserialize map with ReceivedMessage as key`() {
        val mapper = SerializationConstants.mapper
        val map = mapOf(
            ReceivedMessage(
                MessageType.Received.STIMULUS,
                MessageValue("value"),
                IncomingInterface(
                    Protocol("REST"),
                    ProtocolData(
                        mapOf("key" to "value")
                    ),
                )
            ) to "value"
        )
        val serialized = mapper.writeValueAsString(map)
        val deserialized = mapper
            .readerFor(
                mapper.typeFactory.constructMapType(
                    Map::class.java,
                    ReceivedMessage::class.java,
                    String::class.java
                )
            )
            .readValue<Map<ReceivedMessage, String>>(serialized)
        assertThat(map).isEqualTo(deserialized)
    }
}