package de.rwth.swc.interact.domain

import io.github.projectmapk.jackson.module.kogera.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


internal class ProtocolDataTest {

    @Test
    fun `can serialize and deserialize`() {
        val objectMapper = jacksonObjectMapper()
        val protocolData = ProtocolData(
            mapOf(
                "key"  to "value",
                "key2" to "value2"
            )
        )
        val serialized = objectMapper.writeValueAsString(protocolData)
        val deserialized = objectMapper.readValue(serialized, ProtocolData::class.java)
        assertThat(protocolData).isEqualTo(deserialized)
    }
}