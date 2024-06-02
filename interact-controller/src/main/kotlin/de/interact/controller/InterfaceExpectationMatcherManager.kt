package de.interact.controller

import de.interact.domain.expectations.specification.events.InterfaceExpectationAddedEvent
import de.interact.domain.testtwin.api.event.InterfaceAddedToVersionEvent
import de.interact.utbi.InterfaceExpectationMatcher
import jakarta.annotation.PostConstruct
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class InterfaceExpectationMatcherManager(
    private val matchers: List<InterfaceExpectationMatcher> = emptyList()
) {
    @PostConstruct
    fun listAdapters() {
        println("\n\n-------Interface Expectation Matchers ------\n")
        if (matchers.isEmpty()) {
            println("None")
        } else {
            for (matcher in matchers) {
                println(
                    "...Loaded Matcher: "
                            + matcher.name
                            + ": "
                            + matcher.version
                )
            }
        }
        println("\n-------------------------------")
    }

    @Async
    @EventListener
    fun onInterfaceAddedEvent(event: InterfaceAddedToVersionEvent) {
        matchers.firstOrNull { it.canHandle(event) }?.match(event)
            ?: throw IllegalStateException("No matcher found for $event")
    }

    @Async
    @EventListener
    fun onInterfaceExpectationAddedEvent(event: InterfaceExpectationAddedEvent) {
        matchers.firstOrNull { it.canHandle(event) }?.match(event)
            ?: throw IllegalStateException("No matcher found for $event")
    }

}