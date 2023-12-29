package de.rwth.swc.interact.domain.amqp

enum class ExchangeType {
    DIRECT,
    FANOUT,
    TOPIC,
    HEADERS
}