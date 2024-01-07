package de.rwth.swc.interact.controller.persistence.domain

import de.rwth.swc.interact.domain.*
import org.springframework.data.annotation.Version
import org.springframework.data.neo4j.core.schema.*
import java.util.*


const val INCOMING_INTERFACE_NODE_LABEL = "IncomingInterface"
const val INTERFACE_NODE_LABEL = "Interface"

@Node(INTERFACE_NODE_LABEL)
internal abstract class InterfaceEntity<T: ComponentInterface> (
    @Id
    val id: UUID,
    val protocol: String,
    @CompositeProperty(prefix = "protocolData")
    val protocolData: Map<String, String>
) {
    @Version
    var neo4jVersion: Long = 0
        private set
    abstract fun toDomain(): T
}

@Node(INCOMING_INTERFACE_NODE_LABEL)
internal class IncomingInterfaceEntity(
    id: UUID = UUID.randomUUID(),
    protocol: String,
    protocolData: Map<String, String>
): InterfaceEntity<IncomingInterface>(id, protocol, protocolData) {

    override fun toDomain() = IncomingInterface(
        Protocol(protocol),
        ProtocolData(protocolData)
    ).also { it.id = InterfaceId(id) }
}

internal fun IncomingInterface.toEntity() = IncomingInterfaceEntity(
    this.id?.id ?: UUID.randomUUID(),
    this.protocol.protocol,
    this.protocolData.data
)

internal interface IncomingInterfaceEntityNoRelations {
    val id: UUID
    val protocol: String
    val protocolData: Map<String, String>
}

const val OUTGOING_INTERFACE_NODE_LABEL = "OutgoingInterface"

@Node(OUTGOING_INTERFACE_NODE_LABEL)
internal class OutgoingInterfaceEntity(
    id: UUID = UUID.randomUUID(),
    protocol: String,
    protocolData: Map<String, String>
) : InterfaceEntity<OutgoingInterface>(id, protocol, protocolData){
    @Relationship(type = "BOUND_TO")
    var boundTo: Set<InterfaceBinding> = emptySet()
        private set

    fun bind(createdBy: String, incomingInterface: IncomingInterfaceEntity) {
        boundTo = boundTo.plusElement(InterfaceBinding(createdBy, incomingInterface))
    }

    override fun toDomain() = OutgoingInterface(
        Protocol(protocol),
        ProtocolData(protocolData)
    ).also { it.id = InterfaceId(id) }
}

internal fun OutgoingInterface.toEntity() = OutgoingInterfaceEntity(
    this.id?.id ?: UUID.randomUUID(),
    this.protocol.protocol,
    this.protocolData.data
)

internal interface OutgoingInterfaceEntityNoRelations {
    val id: UUID
    val protocol: String
    val protocolData: Map<String, String>
}

@RelationshipProperties
internal data class InterfaceBinding(
    val createdBy: String,
    @TargetNode private val boundTo: IncomingInterfaceEntity
) {
    @RelationshipId
    var id: String? = null
        private set
}