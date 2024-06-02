package de.interact.amqp

import com.fasterxml.jackson.annotation.JsonRawValue

open class AMQPMessage<T : Any>(
    val headers: Map<String, Any>,
    open val body: T
) {
}

class StringAMQPMessage(
    headers: Map<String, Any>,
    @JsonRawValue
    override val body: String
) : AMQPMessage<String>(headers, body) {
}

