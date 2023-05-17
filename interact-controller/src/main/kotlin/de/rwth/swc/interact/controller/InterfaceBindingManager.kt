package de.rwth.swc.interact.controller

import de.rwth.swc.interact.tccii.InterfaceBinder
import jakarta.annotation.PostConstruct
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
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
            neo4jClient.query(
                "MATCH (i:InboundInterface)-[b:BOUND_TO]->(o:OutboundInterface) WHERE NOT b.createdBy IN [" + matchers.stream()
                    .map { "'" + it.name + ":" + it.version + "'" }.collect(Collectors.joining(",")) + "] DELETE b"
            ).run()
            matchers.forEach { it.bindInterfaces() }
        }
        println("\n-------------------------------")
    }

    @Scheduled(fixedDelay = 30000)
    fun match() {
        matchers.forEach { it.bindInterfaces() }
    }
}