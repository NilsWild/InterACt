package de.interact.domain.amqp

sealed interface AmqpProtocolData

class IncomingAmqpProtocolData(
    val queueBindings: List<QueueBinding>
) : AmqpProtocolData

class OutgoingAmqpProtocolData(
    val exchangeName: ExchangeName,
    val exchangeType: ExchangeType,
    val routingKey: RoutingKey,
    val headers: Map<String, String> = emptyMap()
) : AmqpProtocolData

@JvmInline
value class QueueName(val name: String) {
    override fun toString(): String {
        return name
    }
}

@JvmInline
value class ExchangeName(val name: String) {
    override fun toString(): String {
        return name
    }
}

@JvmInline
value class RoutingKey(val key: String) {
    override fun toString(): String {
        return key
    }
}