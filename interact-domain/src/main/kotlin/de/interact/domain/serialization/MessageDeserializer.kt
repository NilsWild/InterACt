package de.interact.domain.serialization

import com.fasterxml.jackson.databind.JavaType
import de.interact.domain.shared.Message

@JvmDefaultWithoutCompatibility
interface MessageDeserializer: Comparable<MessageDeserializer> {

    val order: Int

    fun canHandle(message: Message<String>): Boolean
    fun readBody(value: Message<String>, bodyType: JavaType): Any

    override fun compareTo(other: MessageDeserializer): Int {
        return order.compareTo(other.order)
    }
}