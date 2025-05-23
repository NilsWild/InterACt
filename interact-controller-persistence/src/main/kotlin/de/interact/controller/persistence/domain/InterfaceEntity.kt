package de.interact.controller.persistence.domain

import de.interact.domain.shared.*
import org.springframework.data.annotation.Transient
import org.springframework.data.neo4j.core.schema.CompositeProperty
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship

const val INTERFACE_NODE_LABEL = "Interface"
const val INCOMING_INTERFACE_NODE_LABEL = "IncomingInterface"
const val OUTGOING_INTERFACE_NODE_LABEL = "OutgoingInterface"
const val BOUNT_TO_RELATIONSHIP_LABEL = "BOUND_TO"

@Node(INTERFACE_NODE_LABEL)
sealed class InterfaceEntity: Entity() {

    lateinit var protocol: String

    @CompositeProperty
    lateinit var protocolData: Map<String, String>

    @Transient
    var labels: Set<String> = setOf(INTERFACE_NODE_LABEL)

    @Relationship(type = LISTENING_TO_RELATIONSHIP_LABEL, direction = Relationship.Direction.INCOMING)
    lateinit var consumer: VersionEntity

    @Relationship(type = SENDING_TO_RELATIONSHIP_LABEL, direction = Relationship.Direction.INCOMING)
    lateinit var producer: VersionEntity

}

@Node(INCOMING_INTERFACE_NODE_LABEL)
class IncomingInterfaceEntity : InterfaceEntity() {

    init {
        labels += setOf(INCOMING_INTERFACE_NODE_LABEL)
    }

    @Relationship(type = BOUNT_TO_RELATIONSHIP_LABEL, direction = Relationship.Direction.INCOMING)
    var boundTo: Set<OutgoingInterfaceEntity> = emptySet()

    @Relationship(type = RECEIVED_BY_RELATIONSHIP_LABEL, direction = Relationship.Direction.INCOMING)
    var receivedMessages: Set<ReceivedMessageEntity> = emptySet()
}

@Node(OUTGOING_INTERFACE_NODE_LABEL)
class OutgoingInterfaceEntity : InterfaceEntity() {

    init {
        labels += setOf(OUTGOING_INTERFACE_NODE_LABEL)
    }

    @Relationship(type = BOUNT_TO_RELATIONSHIP_LABEL)
    var boundTo: Set<IncomingInterfaceEntity> = emptySet()

    @Relationship(type = SENT_BY_RELATIONSHIP_LABEL, direction = Relationship.Direction.INCOMING)
    var sentMessages: Set<SentMessageEntity> = emptySet()
}

fun EntityReference<InterfaceId>.toEntity(): InterfaceEntity {
    return when(id) {
        is IncomingInterfaceId -> incomingInterfaceEntityReference(id as IncomingInterfaceId,version)
        is OutgoingInterfaceId -> outgoingInterfaceEntityReference(id as OutgoingInterfaceId, version)
    }
}

fun EntityReference<IncomingInterfaceId>.toEntity(): IncomingInterfaceEntity {
    return incomingInterfaceEntityReference(id,version)
}

fun EntityReference<OutgoingInterfaceId>.toEntity(): OutgoingInterfaceEntity {
    return outgoingInterfaceEntityReference(id,version)
}

fun incomingInterfaceEntityReference(id: IncomingInterfaceId, version: Long?): IncomingInterfaceEntity {
    return IncomingInterfaceEntity().also {
        it.id = id.value
        it.version = version
    }
}

fun incomingInterfaceEntity(id: IncomingInterfaceId, version: Long?, protocol: Protocol, protocolData: ProtocolData): IncomingInterfaceEntity {
    return incomingInterfaceEntityReference(id,version).also {
        it.protocol = protocol.value
        it.protocolData = protocolData.data
    }
}

fun outgoingInterfaceEntityReference(id: OutgoingInterfaceId, version: Long?): OutgoingInterfaceEntity {
    return OutgoingInterfaceEntity().also {
        it.id = id.value
        it.version = version
    }
}

fun outgoingInterfaceEntity(id: OutgoingInterfaceId, version: Long?, protocol: Protocol, protocolData: ProtocolData): OutgoingInterfaceEntity {
    return outgoingInterfaceEntityReference(id,version).also {
        it.protocol = protocol.value
        it.protocolData = protocolData.data
    }
}

interface InterfaceReferenceProjection: EntityReferenceWithLabelsProjection

fun InterfaceReferenceProjection.toEntityReference(): EntityReference<InterfaceId> {
    return when {
        labels.contains(INCOMING_INTERFACE_NODE_LABEL) -> EntityReference(IncomingInterfaceId(id), version)
        labels.contains(OUTGOING_INTERFACE_NODE_LABEL) -> EntityReference(OutgoingInterfaceId(id), version)
        else -> throw IllegalArgumentException("Unknown interface type")
    }
}

interface IncomingInterfaceReferenceProjection: InterfaceReferenceProjection

fun IncomingInterfaceReferenceProjection.toEntityReference(): EntityReference<IncomingInterfaceId> {
    require(labels.contains(INCOMING_INTERFACE_NODE_LABEL))
    return EntityReference(IncomingInterfaceId(id), version)
}

interface OutgoingInterfaceReferenceProjection: InterfaceReferenceProjection

fun OutgoingInterfaceReferenceProjection.toEntityReference(): EntityReference<OutgoingInterfaceId> {
    require(labels.contains(OUTGOING_INTERFACE_NODE_LABEL))
    return EntityReference(OutgoingInterfaceId(id), version)
}