package de.interact.controller.expectations.specification.repository

import de.interact.controller.persistence.domain.*
import de.interact.domain.expectations.specification.collection.*
import de.interact.domain.expectations.specification.spi.ExpectationsCollections
import de.interact.domain.shared.*
import org.springframework.data.neo4j.core.Neo4jTemplate
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import java.util.*

@Repository
interface ExpectationsCollectionsRepository: org.springframework.data.repository.Repository<ExpectationsCollectionEntity, UUID> {
    fun findByNameAndVersionName(name: String, version: String): ExpectationsCollectionProjection?
}

@Service
class ExpectationsCollectionsDao(
    private val neo4jTemplate: Neo4jTemplate,
    private val repository: ExpectationsCollectionsRepository
): ExpectationsCollections {
    override fun save(expectationsCollection: ExpectationsCollection): ExpectationsCollection {
        return neo4jTemplate.saveAs(expectationsCollection.toEntity(), ExpectationsCollectionProjection::class.java)
            .toDomain().copy(postPersistEvents = expectationsCollection.postPersistEvents)
    }

    override fun findByNameAndVersion(
        name: ExpectationsCollectionName,
        version: ExpectationsCollectionVersion
    ): ExpectationsCollection? {
        return repository.findByNameAndVersionName(name.value, version.value)?.toDomain()
    }
}

private fun ExpectationsCollection.toEntity(): ExpectationsCollectionEntity {
    return expectationsCollectionEntity(
        id,
        version,
        name.value,
        versionName.value,
        expectations.map {
            it.toEntity()
        }.toSet()
    )
}

private fun SystemPropertyExpectation.toEntity(): SystemPropertyExpectationEntity {
    return systemPropertyExpectationEntity(
        id,
        version,
        identifier,
        fromInterface.toEntity(),
        toInterface.toEntity()
    )
}

private fun InterfaceExpectation.IncomingInterfaceExpectation.IndirectIncomingInterfaceExpectation.toEntity(): IndirectIncomingInterfaceExpectationEntity {
    return indirectIncomingInterfaceExpectationEntity(
        id,
        version,
        protocol.value,
        protocolData.data
    )
}

private fun InterfaceExpectation.IncomingInterfaceExpectation.DirectIncomingInterfaceExpectation.toEntity(): IncomingInterfaceExpectationEntity {
    return directIncomingInterfaceExpectationEntity(
        id,
        version,
        protocol.value,
        protocolData.data
    )
}

private fun InterfaceExpectation.OutgoingInterfaceExpectation.DirectOutgoingInterfaceExpectation.toEntity(): OutgoingInterfaceExpectationEntity {
    return directOutgoingInterfaceExpectationEntity(
        id,
        version,
        protocol.value,
        protocolData.data
    )
}

private fun InterfaceExpectation.OutgoingInterfaceExpectation.IndirectOutgoingInterfaceExpectation.toEntity(): IndirectOutgoingInterfaceExpectationEntity {
    return indirectOutgoingInterfaceExpectationEntity(
        id,
        version,
        protocol.value,
        protocolData.data
    )
}

private fun InterfaceExpectation.IncomingInterfaceExpectation.toEntity(): IncomingInterfaceExpectationEntity {
    return when (this) {
        is InterfaceExpectation.IncomingInterfaceExpectation.IndirectIncomingInterfaceExpectation -> toEntity()
        is InterfaceExpectation.IncomingInterfaceExpectation.DirectIncomingInterfaceExpectation -> toEntity()
    }
}

private fun InterfaceExpectation.OutgoingInterfaceExpectation.toEntity(): OutgoingInterfaceExpectationEntity {
    return when (this) {
        is InterfaceExpectation.OutgoingInterfaceExpectation.DirectOutgoingInterfaceExpectation -> toEntity()
        is InterfaceExpectation.OutgoingInterfaceExpectation.IndirectOutgoingInterfaceExpectation -> toEntity()
    }
}

interface ExpectationsCollectionProjection: ExpectationsCollectionReferenceProjection {
    val name: String
    val versionName: String
    val expectations: Set<SystemPropertyExpectationProjection>

    interface SystemPropertyExpectationProjection: SystemPropertyExpectationReferenceProjection {
        val identifier: String
        val from: IncomingInterfaceExpectationProjection
        val to: OutgoingInterfaceExpectationProjection

        interface IncomingInterfaceExpectationProjection: IncomingInterfaceExpectationReferenceProjection {
            val protocol: String
            val protocolData: Map<String, String>
        }

        interface OutgoingInterfaceExpectationProjection: OutgoingInterfaceExpectationReferenceProjection {
            val protocol: String
            val protocolData: Map<String, String>
        }
    }
}

private fun ExpectationsCollectionProjection.toDomain(): ExpectationsCollection {
    return ExpectationsCollection(
        ExpectationsCollectionName(name),
        ExpectationsCollectionVersion(versionName),
        expectations.map {
            it.toDomain()
        }.toSet()
    )
}

private fun ExpectationsCollectionProjection.SystemPropertyExpectationProjection.toDomain(): SystemPropertyExpectation {
    return SystemPropertyExpectation(
        SystemPropertyExpectationIdentifier(identifier),
        from.toDomain(),
        to.toDomain(),
        SystemPropertyExpectationId(id),
        version
    )
}

private fun ExpectationsCollectionProjection.SystemPropertyExpectationProjection.IncomingInterfaceExpectationProjection.toDomain(): InterfaceExpectation.IncomingInterfaceExpectation {
    return when {
        labels.contains(DIRECT_INCOMING_INTERFACE_EXPECTATION_NODE_LABEL) -> InterfaceExpectation.IncomingInterfaceExpectation.DirectIncomingInterfaceExpectation(
            Protocol(protocol),
            ProtocolData(protocolData),
            DirectIncomingInterfaceExpectationId(id),
            version
        )
        labels.contains(INDIRECT_INCOMING_INTERFACE_EXPECTATION_NODE_LABEL) -> InterfaceExpectation.IncomingInterfaceExpectation.IndirectIncomingInterfaceExpectation(
            Protocol(protocol),
            ProtocolData(protocolData),
            IndirectIncomingInterfaceExpectationId(id),
            version
        )
        else -> throw IllegalArgumentException("Unknown incoming interface expectation type")
    }
}

private fun ExpectationsCollectionProjection.SystemPropertyExpectationProjection.OutgoingInterfaceExpectationProjection.toDomain(): InterfaceExpectation.OutgoingInterfaceExpectation {
    return when {
        labels.contains(DIRECT_OUTGOING_INTERFACE_EXPECTATION_NODE_LABEL) -> InterfaceExpectation.OutgoingInterfaceExpectation.DirectOutgoingInterfaceExpectation(
            Protocol(protocol),
            ProtocolData(protocolData),
            DirectOutgoingInterfaceExpectationId(id),
            version
        )
        labels.contains(INDIRECT_OUTGOING_INTERFACE_EXPECTATION_NODE_LABEL) -> InterfaceExpectation.OutgoingInterfaceExpectation.IndirectOutgoingInterfaceExpectation(
            Protocol(protocol),
            ProtocolData(protocolData),
            IndirectOutgoingInterfaceExpectationId(id),
            version
        )
        else -> throw IllegalArgumentException("Unknown outgoing interface expectation type")
    }
}

