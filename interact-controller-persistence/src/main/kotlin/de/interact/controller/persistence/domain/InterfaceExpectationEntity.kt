package de.interact.controller.persistence.domain

import de.interact.domain.shared.*
import org.springframework.data.annotation.Transient
import org.springframework.data.neo4j.core.schema.CompositeProperty
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship

const val INTERFACE_EXPECTATION_NODE_LABEL = "InterfaceExpectation"
const val INCOMING_INTERFACE_EXPECTATION_NODE_LABEL = "IncomingInterfaceExpectation"
const val OUTGOING_INTERFACE_EXPECTATION_NODE_LABEL = "OutgoingInterfaceExpectation"
const val DIRECT_INCOMING_INTERFACE_EXPECTATION_NODE_LABEL = "DirectIncomingInterfaceExpectation"
const val INDIRECT_INCOMING_INTERFACE_EXPECTATION_NODE_LABEL = "IndirectIncomingInterfaceExpectation"
const val DIRECT_OUTGOING_INTERFACE_EXPECTATION_NODE_LABEL = "DirectOutgoingInterfaceExpectation"
const val INDIRECT_OUTGOING_INTERFACE_EXPECTATION_NODE_LABEL = "IndirectOutgoingInterfaceExpectation"
const val MATCHED_BY_RELATIONSHIP_LABEL = "MATCHED_BY"

@Node(INTERFACE_EXPECTATION_NODE_LABEL)
sealed class InterfaceExpectationEntity<T : InterfaceEntity>: Entity() {

    lateinit var protocol: String

    @CompositeProperty
    lateinit var protocolData: Map<String, String>

    @Relationship(type = MATCHED_BY_RELATIONSHIP_LABEL)
    open var matches: Set<T> = emptySet()

    @Transient
    var labels = setOf(INTERFACE_EXPECTATION_NODE_LABEL)

}

@Node(INCOMING_INTERFACE_EXPECTATION_NODE_LABEL)
abstract class IncomingInterfaceExpectationEntity :
    InterfaceExpectationEntity<IncomingInterfaceEntity>() {
    init {
        labels += INCOMING_INTERFACE_EXPECTATION_NODE_LABEL
    }
}

@Node(OUTGOING_INTERFACE_EXPECTATION_NODE_LABEL)
abstract class OutgoingInterfaceExpectationEntity :
    InterfaceExpectationEntity<OutgoingInterfaceEntity>() {

    init {
        labels += OUTGOING_INTERFACE_EXPECTATION_NODE_LABEL
    }
}

@Node(DIRECT_INCOMING_INTERFACE_EXPECTATION_NODE_LABEL)
class DirectIncomingInterfaceExpectationEntity :
    IncomingInterfaceExpectationEntity() {

    init {
        labels += DIRECT_INCOMING_INTERFACE_EXPECTATION_NODE_LABEL
    }
}

@Node(INDIRECT_INCOMING_INTERFACE_EXPECTATION_NODE_LABEL)
class IndirectIncomingInterfaceExpectationEntity :
    IncomingInterfaceExpectationEntity() {
    init {
        labels += INDIRECT_INCOMING_INTERFACE_EXPECTATION_NODE_LABEL
    }
}

@Node(DIRECT_OUTGOING_INTERFACE_EXPECTATION_NODE_LABEL)
class DirectOutgoingInterfaceExpectationEntity :
    OutgoingInterfaceExpectationEntity() {

    init {
        labels += DIRECT_OUTGOING_INTERFACE_EXPECTATION_NODE_LABEL
    }
}

@Node(INDIRECT_OUTGOING_INTERFACE_EXPECTATION_NODE_LABEL)
class IndirectOutgoingInterfaceExpectationEntity :
    OutgoingInterfaceExpectationEntity() {

    init {
        labels += INDIRECT_OUTGOING_INTERFACE_EXPECTATION_NODE_LABEL
    }
}

fun indirectIncomingInterfaceExpectationEntityReference(
    id: IncomingInterfaceExpectationId,
    version: Long?
): IndirectIncomingInterfaceExpectationEntity {
    return IndirectIncomingInterfaceExpectationEntity().also {
        it.id = id.id
        it.version = version
    }
}

fun indirectIncomingInterfaceExpectationEntity(
    id: IncomingInterfaceExpectationId,
    version: Long? = null,
    protocol: String,
    protocolData: Map<String, String>
): IndirectIncomingInterfaceExpectationEntity {
    return indirectIncomingInterfaceExpectationEntityReference(id,version).also {
        it.protocol = protocol
        it.protocolData = protocolData
    }
}

fun directIncomingInterfaceExpectationEntityReference(
    id: IncomingInterfaceExpectationId,
    version: Long?
): DirectIncomingInterfaceExpectationEntity {
    return DirectIncomingInterfaceExpectationEntity().also {
        it.id = id.id
        it.version = version
    }
}

fun directIncomingInterfaceExpectationEntity(
    id: IncomingInterfaceExpectationId,
    version: Long? = null,
    protocol: String,
    protocolData: Map<String, String>
): DirectIncomingInterfaceExpectationEntity {
    return directIncomingInterfaceExpectationEntityReference(id,version).also {
        it.protocol = protocol
        it.protocolData = protocolData
    }
}

fun indirectOutgoingInterfaceExpectationEntityReference(
    id: InterfaceExpectationId,
    version: Long?
): IndirectOutgoingInterfaceExpectationEntity {
    return IndirectOutgoingInterfaceExpectationEntity().also {
        it.id = id.id
        it.version = version
    }
}

fun indirectOutgoingInterfaceExpectationEntity(
    id: InterfaceExpectationId,
    version: Long? = null,
    protocol: String,
    protocolData: Map<String, String>
): IndirectOutgoingInterfaceExpectationEntity {
    return indirectOutgoingInterfaceExpectationEntityReference(id,version).also {
        it.protocol = protocol
        it.protocolData = protocolData
    }
}

fun directOutgoingInterfaceExpectationEntityReference(
    id: InterfaceExpectationId,
    version: Long?
): DirectOutgoingInterfaceExpectationEntity {
    return DirectOutgoingInterfaceExpectationEntity().also {
        it.id = id.id
        it.version = version
    }
}

fun directOutgoingInterfaceExpectationEntity(
    id: InterfaceExpectationId,
    version: Long? = null,
    protocol: String,
    protocolData: Map<String, String>
): DirectOutgoingInterfaceExpectationEntity {
    return directOutgoingInterfaceExpectationEntityReference(id,version).also {
        it.protocol = protocol
        it.protocolData = protocolData
    }
}

fun EntityReference<IncomingInterfaceExpectationId>.toEntity(): IncomingInterfaceExpectationEntity {
    return when (id) {
        is DirectIncomingInterfaceExpectationId -> directIncomingInterfaceExpectationEntityReference(id as DirectIncomingInterfaceExpectationId,version)
        is IndirectIncomingInterfaceExpectationId -> indirectIncomingInterfaceExpectationEntityReference(id as IndirectIncomingInterfaceExpectationId,version)
    }
}

fun EntityReference<InterfaceExpectationId>.toEntity(): InterfaceExpectationEntity<*> {
    return when (id) {
        is DirectOutgoingInterfaceExpectationId -> directOutgoingInterfaceExpectationEntityReference(id as DirectOutgoingInterfaceExpectationId,version)
        is IndirectOutgoingInterfaceExpectationId -> indirectOutgoingInterfaceExpectationEntityReference(id as IndirectOutgoingInterfaceExpectationId,version)
        is DirectIncomingInterfaceExpectationId -> directIncomingInterfaceExpectationEntityReference(id as DirectIncomingInterfaceExpectationId,version)
        is IndirectIncomingInterfaceExpectationId -> indirectIncomingInterfaceExpectationEntityReference(id as IndirectIncomingInterfaceExpectationId,version)
    }
}

fun EntityReference<OutgoingInterfaceExpectationId>.toEntity(): OutgoingInterfaceExpectationEntity {
    return when (id) {
        is DirectOutgoingInterfaceExpectationId -> directOutgoingInterfaceExpectationEntityReference(id as DirectOutgoingInterfaceExpectationId,version)
        is IndirectOutgoingInterfaceExpectationId -> indirectOutgoingInterfaceExpectationEntityReference(id as IndirectOutgoingInterfaceExpectationId,version)
    }
}

interface InterfaceExpectationReferenceProjection: EntityReferenceWithLabelsProjection

interface IncomingInterfaceExpectationReferenceProjection: InterfaceExpectationReferenceProjection

interface OutgoingInterfaceExpectationReferenceProjection: InterfaceExpectationReferenceProjection

interface DirectIncomingInterfaceExpectationReferenceProjection: IncomingInterfaceExpectationReferenceProjection

interface IndirectIncomingInterfaceExpectationReferenceProjection: IncomingInterfaceExpectationReferenceProjection

interface DirectOutgoingInterfaceExpectationReferenceProjection: OutgoingInterfaceExpectationReferenceProjection

interface IndirectOutgoingInterfaceExpectationReferenceProjection: OutgoingInterfaceExpectationReferenceProjection