package de.interact.amqp.observer

import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessagePostProcessor

class TestClientPublishPostProcessor : MessagePostProcessor {
    override fun postProcessMessage(message: Message): Message {
        AmqpObserverLatch.increment()
        message.messageProperties.setHeader("interact.sender.type", "TEST")
        return message
    }
}

