package de.rwth.swc.interact.controller.persistence.domain

import de.rwth.swc.interact.domain.SystemPropertyExpectation
import de.rwth.swc.interact.domain.SystemPropertyExpectationId
import de.rwth.swc.interact.domain.SystemPropertyExpectationName
import de.rwth.swc.interact.domain.SystemPropertyExpectationSource
import org.springframework.data.annotation.Version
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship
import java.util.*

const val SYSTEM_PROPERTY_EXPECTATION_NODE_LABEL = "SystemPropertyExpectation"

@Node(SYSTEM_PROPERTY_EXPECTATION_NODE_LABEL)
internal class SystemPropertyExpectationEntity (
    @Id
    val id: SystemPropertyExpectationId,
    val source: String,
    val name: String,
    @Relationship(type = "EXPECT_FROM_MATCHING")
    val fromInterface: OutgoingInterfaceExpectationEntity,
    @Relationship(type = "EXPECT_TO_MATCHING")
    val toInterface: IncomingInterfaceExpectationEntity
) {
    var validated: Boolean? = null

    @Version
    var neo4jVersion: Long = 0
        private set

    fun toDomain() = SystemPropertyExpectation(
        SystemPropertyExpectationSource(source),
        SystemPropertyExpectationName(name)
    ).also {
        it.id = this.id
        it.validated = this.validated
        it.fromInterface = fromInterface.toDomain()
        it.toInterface = toInterface.toDomain()
    }
}

internal fun SystemPropertyExpectation.toEntity() = SystemPropertyExpectationEntity(
    this.id ?: SystemPropertyExpectationId.random(),
    this.source.source,
    this.name.name,
    this.fromInterface!!.toEntity(),
    this.toInterface!!.toEntity()
).also { entity ->
    entity.validated = this.validated
}

internal interface SystemPropertyExpectationEntityNoRelationships {
    val id: UUID
    val source: String
    val name: String
}