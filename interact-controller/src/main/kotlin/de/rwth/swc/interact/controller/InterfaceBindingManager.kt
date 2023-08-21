package de.rwth.swc.interact.controller

import de.rwth.swc.interact.controller.persistence.events.InterfaceAddedEvent
import de.rwth.swc.interact.utbi.InterfaceBinder
import jakarta.annotation.PostConstruct
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.event.TransactionalEventListener
import java.util.stream.Collectors

@Service
class InterfaceBindingManager(
    private val matchers: List<InterfaceBinder> = emptyList(),
    private val neo4jClient: Neo4jClient
) {
    @PostConstruct
    fun listAdapters() {
        println("\n\n-------Interface Binders ------\n")
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
        matchers.firstOrNull{ it.canHandle(event.componentInterface) }?.bindInterfaces(event.componentInterface)
            ?: throw IllegalStateException("No matcher found for ${event.componentInterface}")
    }
}