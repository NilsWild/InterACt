package de.interact.amqp

import de.interact.domain.amqp.AmqpMessage
import org.springframework.amqp.rabbit.core.RabbitTemplate

class TestAmqpClient(
    private val rabbitTemplate: RabbitTemplate
) {
    fun send(exchange: String, routingKey: String, message: AmqpMessage<*>) {
        rabbitTemplate.convertAndSend(
            exchange,
            routingKey,
            message.body
        )
        {
            it.messageProperties.headers = message.headers
            it
        }
    }

    fun <T> receive(queueName: String): T? {
        @Suppress("UNCHECKED_CAST")
        return rabbitTemplate.receiveAndConvert(queueName) as T?
    }
}