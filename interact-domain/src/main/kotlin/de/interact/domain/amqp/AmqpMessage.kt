package de.interact.domain.amqp

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import de.interact.domain.shared.Message

@JsonSerialize(using = AmqpMessageSerializer::class)
@JsonDeserialize(using = AmqpMessageDeserializer::class)
data class AmqpMessage<T: Any>(
    val headers: Map<String, Any>,
    override val body: T
): Message<T>
