package de.rwth.swc.interact.controller.persistence.domain

import org.springframework.data.annotation.Version
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship
import java.util.*

@Node
data class Component internal constructor(
    @Id
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val version: String
) {
    @Relationship(type = "TESTED_BY")
    var abstractTestCases: Set<AbstractTestCase> = emptySet()
        private set

    @Relationship(type = "PROVIDES")
    var providedInterfaces: Set<IncomingInterface> = emptySet()
        private set

    @Relationship(type = "REQUIRES")
    var requiredInterfaces: Set<OutgoingInterface> = emptySet()
        private set

    @Version
    var neo4jVersion: Long = 0
        private set

    fun abstractTestCase(
        id: UUID = UUID.randomUUID(),
        name: String,
        src: String,
        init: (AbstractTestCase.() -> Unit)? = null
    ) = AbstractTestCase(id, name, src).also {
        if (init != null) {
            it.init()
        }
        abstractTestCases = abstractTestCases.plusElement(it)
    }

    fun incomingInterface(
        id: UUID = UUID.randomUUID(),
        protocol: String,
        protocolData: Map<String, String>
    ) = IncomingInterface(id, protocol, protocolData).also {
        providedInterfaces = providedInterfaces.plusElement(it)
    }

    fun outgoingInterface(
        id: UUID = UUID.randomUUID(),
        protocol: String,
        protocolData: Map<String, String>
    ) = OutgoingInterface(id, protocol, protocolData).also {
        requiredInterfaces = requiredInterfaces.plusElement(it)
    }

}

fun component(
    id: UUID = UUID.randomUUID(),
    name: String,
    version: String,
    init: (Component.() -> Unit)? = null
) = Component(id, name, version).also {
    if (init != null) {
        it.init()
    }
}