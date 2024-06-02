package de.interact.utbi

import de.interact.domain.expectations.specification.events.InterfaceExpectationAddedEvent
import de.interact.domain.shared.InterfaceExpectationId
import de.interact.domain.shared.InterfaceId
import de.interact.domain.testtwin.api.event.InterfaceAddedToVersionEvent

interface InterfaceExpectationMatcher {
    val name: InterfaceExpectationMatcherName
    val version: InterfaceExpectationMatcherVersion

    fun canHandle(event: InterfaceExpectationAddedEvent): Boolean
    fun canHandle(event: InterfaceAddedToVersionEvent): Boolean
    fun match(event: InterfaceExpectationAddedEvent): List<Pair<InterfaceExpectationId, InterfaceId>>
    fun match(event: InterfaceAddedToVersionEvent): List<Pair<InterfaceExpectationId, InterfaceId>>
}

@JvmInline
value class InterfaceExpectationMatcherName(val name: String) {
    override fun toString(): String {
        return name
    }
}

@JvmInline
value class InterfaceExpectationMatcherVersion(val version: String) {
    override fun toString(): String {
        return version
    }
}
