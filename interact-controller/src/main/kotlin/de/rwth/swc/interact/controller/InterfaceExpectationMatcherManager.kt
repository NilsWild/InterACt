package de.rwth.swc.interact.controller

import de.rwth.swc.interact.controller.persistence.events.InterfaceAddedEvent
import de.rwth.swc.interact.controller.persistence.events.InterfaceExpectationAddedEvent
import de.rwth.swc.interact.utbi.InterfaceExpectationMatcher
import jakarta.annotation.PostConstruct
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.event.TransactionalEventListener

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
    @TransactionalEventListener
    fun onInterfaceAddedEvent(event: InterfaceAddedEvent) {
        matchers.firstOrNull{ it.canHandle(event.componentInterface) }?.match(event.componentInterface)
            ?: throw IllegalStateException("No interface expectation matcher found for ${event.componentInterface}")
    }

    @Async
    @TransactionalEventListener
    fun onInterfaceAddedEvent(event: InterfaceExpectationAddedEvent) {
        matchers.firstOrNull{ it.canHandle(event.interfaceExpectation) }?.match(event.interfaceExpectation)
            ?: throw IllegalStateException("No interface expectation matcher found for ${event.interfaceExpectation}")
    }

}