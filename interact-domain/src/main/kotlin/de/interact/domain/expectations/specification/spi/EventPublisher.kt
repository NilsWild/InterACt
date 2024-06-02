package de.interact.domain.expectations.specification.spi

import de.interact.domain.expectations.specification.events.SpecificationEvent

interface EventPublisher {
    fun publish(event: SpecificationEvent)
}