package de.interact.domain.amqp

enum class ExchangeType {
    DIRECT,
    FANOUT,
    TOPIC,
    HEADERS
}