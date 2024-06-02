package de.interact.controller.persistence.domain

import de.interact.domain.shared.EntityReference
import de.interact.domain.shared.EntityReferenceProjection
import de.interact.domain.shared.SystemPropertyExpectationId
import de.interact.domain.shared.SystemPropertyExpectationIdentifier
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship

const val SYSTEM_PROPERTY_EXPECTATION_NODE_LABEL = "SystemPropertyExpectation"
const val FOUND_EXPECTATIONS_RELATIONSHIP_LABEL = "FOUND_EXPECTATIONS"
const val INTERACTION_STIMULUS_RELATIONSHIP_LABEL = "INTERACTION_STIMULUS"
const val INTERACTION_REACTION_RELATIONSHIP_LABEL = "INTERACTION_REACTION"

@Node(SYSTEM_PROPERTY_EXPECTATION_NODE_LABEL)
class SystemPropertyExpectationEntity: Entity() {

    lateinit var identifier: String

    @Relationship(INTERACTION_STIMULUS_RELATIONSHIP_LABEL)
    lateinit var from: IncomingInterfaceExpectationEntity

    @Relationship(INTERACTION_REACTION_RELATIONSHIP_LABEL)
    lateinit var to: OutgoingInterfaceExpectationEntity

    @Relationship(FOUND_EXPECTATIONS_RELATIONSHIP_LABEL)
    var derivedExpectations: Set<SystemInteractionExpectationEntity> = emptySet()

}

fun systemPropertyExpectationEntityReference(
    id: SystemPropertyExpectationId,
    version: Long?
): SystemPropertyExpectationEntity {
    return SystemPropertyExpectationEntity().also {
        it.id = id.value
        it.version = version
    }
}

fun systemPropertyExpectationEntity(
    id: SystemPropertyExpectationId,
    version: Long? = null,
    identifier: SystemPropertyExpectationIdentifier,
    from: IncomingInterfaceExpectationEntity,
    to: OutgoingInterfaceExpectationEntity,
    derivedExpectations: Set<SystemInteractionExpectationEntity> = emptySet()
): SystemPropertyExpectationEntity {
    return systemPropertyExpectationEntityReference(id,version).also {
        it.identifier = identifier.value
        it.from = from
        it.to = to
        it.derivedExpectations = derivedExpectations
    }
}

interface SystemPropertyExpectationReferenceProjection: EntityReferenceProjection

fun SystemPropertyExpectationReferenceProjection.toEntityReference(): EntityReference<SystemPropertyExpectationId> {
    return EntityReference(SystemPropertyExpectationId(id),version)
}

fun EntityReference<SystemPropertyExpectationId>.toEntity(): SystemPropertyExpectationEntity {
    return systemPropertyExpectationEntityReference(id, version)
}