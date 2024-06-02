package de.interact.controller.persistence.domain

import de.interact.domain.shared.VersionId
import org.springframework.data.annotation.Version
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship
import java.util.*

const val VERSION_NODE_LABEL = "Version"
const val VERSION_OF_RELATIONSHIP_LABEL = "VERSION_OF"
const val TESTED_BY_RELATIONSHIP_LABEL = "TESTED_BY"
const val LISTENING_TO_RELATIONSHIP_LABEL = "LISTENING_TO"
const val SENDING_TO_RELATIONSHIP_LABEL = "SENDING_TO"

@Node(VERSION_NODE_LABEL)
class VersionEntity {

    constructor(id: VersionId, identifier: String, versionOf: ComponentEntity, version: Long? = null) {
        this.id = id.value
        this.identifier = identifier
        this.versionOf = versionOf
        this.version = version
    }

    lateinit var identifier: String

    @Relationship(type = VERSION_OF_RELATIONSHIP_LABEL)
    lateinit var versionOf: ComponentEntity

    @Id
    lateinit var id: UUID

    @Relationship(type = TESTED_BY_RELATIONSHIP_LABEL)
    var testedBy: Set<AbstractTestCaseEntity> = setOf()

    @Relationship(type = LISTENING_TO_RELATIONSHIP_LABEL)
    var listeningTo: Set<IncomingInterfaceEntity> = setOf()

    @Relationship(type = SENDING_TO_RELATIONSHIP_LABEL)
    var sendingTo: Set<OutgoingInterfaceEntity> = setOf()

    @Version
    var version: Long? = null

}