package de.rwth.swc.interact.amqp.observer

import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessagePostProcessor
import java.util.*

object MessageDropper {

    private val messagesToBeDroppedQueue = LinkedList<Boolean>()

    fun addMessageToBeDropped() {
        messagesToBeDroppedQueue.offer(true)
    }

    fun addMessageToBeRecorded() {
        messagesToBeDroppedQueue.offer(false)
    }

    fun shouldMessageBeDropped(): Boolean {
        return messagesToBeDroppedQueue.pop()
    }

}

class MessageDropperPostProcessor : MessagePostProcessor {
    override fun postProcessMessage(message: Message): Message {
        MessageDropper.addMessageToBeDropped()
        return message
    }
}
