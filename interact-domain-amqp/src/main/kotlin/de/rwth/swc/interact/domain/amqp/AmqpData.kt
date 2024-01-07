package de.rwth.swc.interact.domain.amqp

import de.rwth.swc.interact.domain.ProtocolData
import de.rwth.swc.interact.domain.serialization.SerializationConstants

data class AmqpData (
    val exchangeName: String?,
    val exchangeType: ExchangeType?,
    val routingKey: String?,
    val headers: Map<String, String> = HashMap(),
    val queueBindings: List<QueueBinding> = emptyList()
) {
    fun toProtocolData() : ProtocolData {
        val data = listOfNotNull(
            this.exchangeName?.let{"exchangeName" to it},
            this.exchangeType?.name?.let{"exchangeType" to it},
            this.routingKey?.let {"routingKey" to it},
            "headers" to SerializationConstants.mapper.writeValueAsString(this.headers),
            "queueBindings" to SerializationConstants.mapper.writeValueAsString(this.queueBindings)
        ).toMap()
        return ProtocolData(
            data
        )
    }
}

fun ProtocolData.toAmqpData() : AmqpData {
    return AmqpData(
        exchangeName = this.data["exchangeName"],
        exchangeType = this.data["exchangeType"]?.let {ExchangeType.valueOf(it)},
        routingKey = this.data["routingKey"],
        headers = this.data["headers"]?.let { SerializationConstants.mapper.readValue(it.replace("'", "\""), SerializationConstants.mapper.typeFactory.constructMapType(Map::class.java, String::class.java, String::class.java)) } ?: HashMap(),
        queueBindings = this.data["queueBindings"]?.let { SerializationConstants.mapper.readValue(it.replace("\'", "\""), SerializationConstants.mapper.typeFactory.constructCollectionType(List::class.java, QueueBinding::class.java)) } ?: emptyList()
    )
}