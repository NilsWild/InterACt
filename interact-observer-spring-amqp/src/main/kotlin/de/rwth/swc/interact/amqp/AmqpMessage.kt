package de.rwth.swc.interact.amqp

import com.fasterxml.jackson.annotation.JsonRawValue

open class AMQPMessage<T>(
    val headers: Map<*,*>,
    open val body: T
) {
}

class StringAMQPMessage(
    headers: Map<*,*>,
    @JsonRawValue
    override val body: String
) : AMQPMessage<String>(headers, body) {
}

