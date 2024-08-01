package de.interact.domain.serialization

import de.interact.domain.shared.Message

@JvmDefaultWithoutCompatibility
interface MessageSerializer: Comparable<MessageSerializer> {

    val order: Int

    fun canHandle(message: Message<*>): Boolean
    fun writeBodyAsString(value: Message<*>): String?

    override fun compareTo(other: MessageSerializer): Int {
        return order.compareTo(other.order)
    }
}