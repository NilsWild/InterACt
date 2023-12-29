package de.rwth.swc.interact.domain.amqp

import de.rwth.swc.interact.domain.ProtocolData
import de.rwth.swc.interact.domain.serialization.SerializationConstants

data class AMQPData (
    val exchangeName: String,
    val exchangeType: ExchangeType,
    val routingKey: String,
    val headers: Map<String, String> = HashMap(),
    val queueBindings: List<QueueBinding> = emptyList()
) {
    fun toProtocolData() : ProtocolData {
        return ProtocolData(
            mapOf(
                "exchangeName" to this.exchangeName,
                "exchangeType" to this.exchangeType.name,
                "routingKey" to this.routingKey,
                "headers" to SerializationConstants.mapper.writeValueAsString(this.headers),
                "queueBindings" to SerializationConstants.mapper.writeValueAsString(this.queueBindings)
            )
        )
    }
}

fun ProtocolData.toAmqpData() : AMQPData {
    return AMQPData(
        exchangeName = this.data["exchangeName"]!!,
        exchangeType = ExchangeType.valueOf(this.data["exchangeType"]!!),
        routingKey = this.data["routingKey"]!!,
        headers = this.data["headers"]?.let { SerializationConstants.mapper.readValue(it.replace("'", "\""), SerializationConstants.mapper.typeFactory.constructMapType(Map::class.java, String::class.java, String::class.java)) } ?: HashMap(),
        queueBindings = this.data["queueBindings"]?.let { SerializationConstants.mapper.readValue(it.replace("\'", "\""), SerializationConstants.mapper.typeFactory.constructCollectionType(List::class.java, QueueBinding::class.java)) } ?: emptyList()
    )
}