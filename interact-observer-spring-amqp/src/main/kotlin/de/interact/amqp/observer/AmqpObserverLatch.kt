package de.interact.amqp.observer

import de.interact.domain.testobservation.spi.MessageObserver
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessagePostProcessor
import java.util.concurrent.atomic.AtomicInteger

object AmqpObserverLatch : MessageObserver {
    private val messagesToBeObserved: AtomicInteger = AtomicInteger(0)

    fun increment() {
        messagesToBeObserved.incrementAndGet()
    }

    fun decrement() {
        messagesToBeObserved.decrementAndGet()
    }

    override fun isFinished(): Boolean {
        return messagesToBeObserved.get() == 0
    }

}

class ObservationPublishPostProcessor : MessagePostProcessor {
    override fun postProcessMessage(message: Message): Message {
        AmqpObserverLatch.increment()
        message.messageProperties.setHeader("interact.sender.type", "CUT")
        return message
    }
}