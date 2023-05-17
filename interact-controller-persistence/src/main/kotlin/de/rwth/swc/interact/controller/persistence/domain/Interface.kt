package de.rwth.swc.interact.controller.persistence.domain

import org.springframework.data.annotation.Version
import org.springframework.data.neo4j.core.schema.*
import java.util.*


@Node("IncomingInterface", "Interface")
open class IncomingInterface(
    @Id
    val id: UUID = UUID.randomUUID(),
    val protocol: String,
    @CompositeProperty(prefix = "protocolData")
    val protocolData: Map<String, String>
) {

    @Version
    var neo4jVersion: Long = 0
        private set

}

@Node("OutgoingInterface", "Interface")
open class OutgoingInterface(
    @Id
    val id: UUID = UUID.randomUUID(),
    val protocol: String,
    @CompositeProperty(prefix = "protocolData")
    val protocolData: Map<String, String>
) {
    @Relationship(type = "BOUND_TO")
    var boundTo: Set<InterfaceBinding> = emptySet()
        private set

    @Version
    var neo4jVersion: Long = 0
        private set

    fun bind(createdBy: String, incomingInterface: IncomingInterface) {
        boundTo = boundTo.plusElement(InterfaceBinding(createdBy, incomingInterface))
    }
}

@RelationshipProperties
data class InterfaceBinding(
    val createdBy: String,
    @TargetNode private val boundTo: IncomingInterface
) {
    @RelationshipId
    var id: Long? = null
        private set

}