package de.rwth.swc.interact.controller.persistence.repository

import de.rwth.swc.interact.controller.persistence.domain.Component
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ComponentRepository : org.springframework.data.repository.Repository<Component, UUID> {

    @Query(
        value = "MATCH (c:Component) " +
                "WHERE c.id=\$componentId " +
                "WITH c " +
                "MATCH (atc:AbstractTestCase) " +
                "WHERE atc.id=\$abstractTestCaseId " +
                "MERGE (c)-[:TESTED_BY]->(atc)"
    )
    fun addAbstractTestCase(
        @Param("componentId") componentId: UUID,
        @Param("abstractTestCaseId") abstractTestCaseId: UUID
    )

    @Query(
        value = "MATCH (c:Component) " +
                "WHERE c.name=\$name AND c.version=\$version " +
                "RETURN c.id"
    )
    fun findIdByNameAndVersion(@Param("name") name: String, @Param("version") version: String): UUID?

    fun save(component: Component): Component

    fun findAll(): List<Component>

    @Query(
        value = "MATCH (c:Component)-[*]->(s) " +
                "WHERE c.id=\$componentId " +
                "DETACH DELETE c, s"
    )
    fun deleteById(
        @Param("componentId") componentId: UUID
    )

    @Query(
        value = "MATCH (c:Component) " +
                "WITH c " +
                "MATCH (c)-[*]->(s) " +
                "DETACH DELETE c, s"
    )
    fun deleteAll()

    @Query(
        value = "MATCH (c:Component), (ii:IncomingInterface) " +
                "WHERE c.id=\$componentId AND ii.id=\$interfaceId " +
                "MERGE (c)-[:PROVIDES]->(ii)"
    )
    fun addProvidedInterface(@Param("componentId") componentId: UUID, @Param("interfaceId") interfaceId: UUID)

    @Query(
        value = "MATCH (c:Component), (oi:OutgoingInterface) " +
                "WHERE c.id=\$componentId AND oi.id=\$interfaceId " +
                "MERGE (c)-[:REQUIRES]->(oi)"
    )
    fun addRequiredInterface(@Param("componentId") componentId: UUID, @Param("interfaceId") interfaceId: UUID)
}