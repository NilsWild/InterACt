package de.interact.domain.serialization

import java.lang.reflect.Type

interface MessageMapper: Comparable<MessageMapper> {

    val order: Int

    fun canHandle(type: Class<*>): Boolean
    fun readValue(value: String, type: Type): Any?
    fun writeValueAsJsonString(value: Any): String

    override fun compareTo(other: MessageMapper): Int {
        return order.compareTo(other.order)
    }
}