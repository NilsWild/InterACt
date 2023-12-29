package de.rwth.swc.interact.amqp.observer

import de.rwth.swc.interact.observer.ObservationLatch
import org.awaitility.Awaitility
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessagePostProcessor
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpoint
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

object AmqpObserverLatch: ObservationLatch {
    private val messagesToBeObserved: AtomicInteger = AtomicInteger(0)

    fun increment() {
        this.messagesToBeObserved.incrementAndGet()
    }

    fun decrement() {
        this.messagesToBeObserved.decrementAndGet()
    }

    override fun isReleased(): Boolean {
        Awaitility
            .await()
            .atMost(5000, TimeUnit.MILLISECONDS)
            .until { this.messagesToBeObserved.get() == 0 }
        return true
    }
}

class ObserverLatchMessagePostProcessor : MessagePostProcessor {
    override fun postProcessMessage(message: Message): Message {
        if (message.messageProperties.receivedExchange != "amq.rabbitmq.trace") {
            AmqpObserverLatch.increment()
            MessageDropper.addMessageToBeRecorded()
        }
        return message
    }
}

class ObserverLatchRabbitListenerContainerFactory :
    SimpleRabbitListenerContainerFactory() {
    override fun initializeContainer(instance: SimpleMessageListenerContainer, endpoint: RabbitListenerEndpoint) {
        super.initializeContainer(instance, endpoint)
        instance.addAfterReceivePostProcessors(ObserverLatchMessagePostProcessor())
    }
}
