package de.rwth.swc.interact.domain.amqp

data class QueueBinding(
    val source: String,
    val routingKey: String,
    val arguments: Map<String, String> = HashMap()
)

