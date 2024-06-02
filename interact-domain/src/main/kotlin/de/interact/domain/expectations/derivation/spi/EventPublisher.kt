package de.interact.domain.expectations.derivation.spi

import de.interact.domain.expectations.derivation.events.DerivationEvent

interface EventPublisher {
    fun publish(event: DerivationEvent)
}