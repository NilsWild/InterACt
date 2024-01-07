package de.rwth.swc.interact.controller.persistence.domain

import de.rwth.swc.interact.domain.*
import org.springframework.data.annotation.Version
import org.springframework.data.neo4j.core.schema.CompositeProperty
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship
import java.util.*

const val INTERFACE_EXPECTATION_NODE_LABEL = "InterfaceExpectation"

@Node(INTERFACE_EXPECTATION_NODE_LABEL)
internal abstract class InterfaceExpectationEntity<T: InterfaceExpectation>(
    @Id
    val id: UUID,
    val protocol: String,
    @CompositeProperty(prefix = "protocolData")
    val protocolData: Map<String, String>
) {

    @Relationship(type = "MATCHED_BY")
    var matches: Set<InterfaceEntity<*>> = emptySet()

    @Version
    var neo4jVersion: Long = 0
        private set
    abstract fun toDomain(): T
}

internal fun InterfaceExpectation.toEntity(): InterfaceExpectationEntity<*> {
    return when(this) {
        is IncomingInterfaceExpectation -> this.toEntity()
        is OutgoingInterfaceExpectation -> this.toEntity()
    }
}

const val INCOMING_INTERFACE_EXPECTATION_NODE_LABEL = "IncomingInterfaceExpectation"

@Node(INCOMING_INTERFACE_EXPECTATION_NODE_LABEL)
internal class IncomingInterfaceExpectationEntity(
    id: UUID = UUID.randomUUID(),
    protocol: String,
    protocolData: Map<String, String>
): InterfaceExpectationEntity<IncomingInterfaceExpectation>(id, protocol, protocolData) {

    override fun toDomain() = IncomingInterfaceExpectation(
        Protocol(protocol),
        ProtocolData(protocolData)
    ).also { it.id = InterfaceExpectationId(id) }
}

internal fun IncomingInterfaceExpectation.toEntity() = IncomingInterfaceExpectationEntity(
    this.id?.id ?: UUID.randomUUID(),
    this.protocol.protocol,
    this.protocolData.data
)

const val OUTGOING_INTERFACE_EXPECTATION_NODE_LABEL = "OutgoingInterfaceExpectation"

@Node(OUTGOING_INTERFACE_EXPECTATION_NODE_LABEL)
internal class OutgoingInterfaceExpectationEntity(
    id: UUID = UUID.randomUUID(),
    protocol: String,
    protocolData: Map<String, String>
) : InterfaceExpectationEntity<OutgoingInterfaceExpectation>(id, protocol, protocolData){
    override fun toDomain() = OutgoingInterfaceExpectation(
        Protocol(protocol),
        ProtocolData(protocolData)
    ).also { it.id = InterfaceExpectationId(id) }
}

internal fun OutgoingInterfaceExpectation.toEntity() = OutgoingInterfaceExpectationEntity(
    this.id?.id ?: UUID.randomUUID(),
    this.protocol.protocol,
    this.protocolData.data
)
