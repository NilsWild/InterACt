package de.interact.controller.persistence.domain

import de.interact.domain.shared.EntityReference
import de.interact.domain.shared.EntityReferenceProjection
import de.interact.domain.shared.VersionId
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship

const val VERSION_NODE_LABEL = "Version"
const val VERSION_OF_RELATIONSHIP_LABEL = "VERSION_OF"
const val TESTED_BY_RELATIONSHIP_LABEL = "TESTED_BY"
const val LISTENING_TO_RELATIONSHIP_LABEL = "LISTENING_TO"
const val SENDING_TO_RELATIONSHIP_LABEL = "SENDING_TO"

@Node(VERSION_NODE_LABEL)
class VersionEntity(): Entity(){

    constructor(id: VersionId, identifier: String, versionOf: ComponentEntity, version: Long? = null): this() {
        this.id = id.value
        this.identifier = identifier
        this.versionOf = versionOf
        this.version = version
    }

    var identifier: String? = null

    @Relationship(type = VERSION_OF_RELATIONSHIP_LABEL)
    var versionOf: ComponentEntity? = null

    @Relationship(type = TESTED_BY_RELATIONSHIP_LABEL)
    var testedBy: Set<AbstractTestCaseEntity> = emptySet()

    @Relationship(type = LISTENING_TO_RELATIONSHIP_LABEL)
    var listeningTo: Set<IncomingInterfaceEntity> = emptySet()

    @Relationship(type = SENDING_TO_RELATIONSHIP_LABEL)
    var sendingTo: Set<OutgoingInterfaceEntity> = emptySet()

}

interface VersionReferenceProjection: EntityReferenceProjection

fun VersionReferenceProjection.toEntityReference(): EntityReference<VersionId> {
    return EntityReference(VersionId(id), version)
}