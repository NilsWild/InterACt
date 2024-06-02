package de.interact.domain.amqp

data class QueueBinding(
    val source: ExchangeName,
    val routingKey: RoutingKey,
    val arguments: Map<String, String> = HashMap()
)

