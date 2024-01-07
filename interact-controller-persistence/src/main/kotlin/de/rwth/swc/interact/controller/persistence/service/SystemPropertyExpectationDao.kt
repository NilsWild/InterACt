package de.rwth.swc.interact.controller.persistence.service

import de.rwth.swc.interact.controller.persistence.domain.SystemPropertyExpectationEntityNoRelationships
import de.rwth.swc.interact.controller.persistence.domain.toEntity
import de.rwth.swc.interact.controller.persistence.repository.SystemPropertyExpectationRepository
import de.rwth.swc.interact.domain.*
import org.springframework.data.neo4j.core.Neo4jTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface SystemPropertyExpectationDao {
    fun findByComponentIdAndSourceAndName(
        componentId: ComponentId,
        source: SystemPropertyExpectationSource,
        name: SystemPropertyExpectationName
    ): SystemPropertyExpectation?
    fun save(systemPropertyExpectation: SystemPropertyExpectation): SystemPropertyExpectationId
    fun addExpectation(id: SystemPropertyExpectationId, interfaceExpectationId: InterfaceExpectationId, expectationType: Class<out InterfaceExpectation>)
}

@Service
@Transactional
internal class SystemPropertyExpectationDaoImpl(
    private val neo4jTemplate: Neo4jTemplate,
    private val systemPropertyExpectationRepository: SystemPropertyExpectationRepository
) : SystemPropertyExpectationDao {
    override fun findByComponentIdAndSourceAndName(
        componentId: ComponentId,
        source: SystemPropertyExpectationSource,
        name: SystemPropertyExpectationName
    ): SystemPropertyExpectation? {
        return systemPropertyExpectationRepository.findIdByComponentIdSourceAndName(componentId.id, source.source, name.name)
            ?.toDomain()
    }

    override fun save(systemPropertyExpectation: SystemPropertyExpectation): SystemPropertyExpectationId {
        return SystemPropertyExpectationId(
            neo4jTemplate.saveAs(
                systemPropertyExpectation.toEntity(),
                SystemPropertyExpectationEntityNoRelationships::class.java
            ).id
        )
    }

    override fun addExpectation(
        id: SystemPropertyExpectationId,
        interfaceExpectationId: InterfaceExpectationId,
        expectationType: Class<out InterfaceExpectation>
    ) {
        when(expectationType) {
            IncomingInterfaceExpectation::class.java -> systemPropertyExpectationRepository.addToExpectation(id.id, interfaceExpectationId.id)
            OutgoingInterfaceExpectation::class.java -> systemPropertyExpectationRepository.addFromExpectation(id.id, interfaceExpectationId.id)
            else -> throw IllegalArgumentException("Unknown interface expectation type: $expectationType")
        }
    }
}