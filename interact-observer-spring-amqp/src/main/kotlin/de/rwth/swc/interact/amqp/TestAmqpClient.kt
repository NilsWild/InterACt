package de.rwth.swc.interact.amqp

import org.springframework.amqp.rabbit.core.RabbitTemplate

class TestAmqpClient (
    private val rabbitTemplate: RabbitTemplate
){
    fun send(exchange: String, routingKey: String, message: AMQPMessage<*>) {
        rabbitTemplate.convertAndSend(
            exchange,
            routingKey,
            message.body)
            {
                it.messageProperties.headers = message.headers
                it
            }
    }
}