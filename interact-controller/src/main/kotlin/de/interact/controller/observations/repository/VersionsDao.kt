package de.interact.controller.observations.repository

import de.interact.controller.persistence.domain.*
import de.interact.domain.shared.ComponentId
import de.interact.domain.shared.VersionId
import de.interact.domain.testtwin.Version
import de.interact.domain.testtwin.abstracttest.concretetest.InteractionTest
import de.interact.domain.testtwin.abstracttest.concretetest.UnitTest
import de.interact.domain.testtwin.abstracttest.concretetest.message.ComponentResponseMessage
import de.interact.domain.testtwin.abstracttest.concretetest.message.EnvironmentResponseMessage
import de.interact.domain.testtwin.abstracttest.concretetest.message.StimulusMessage
import de.interact.domain.testtwin.spi.Versions
import org.springframework.data.neo4j.core.Neo4jTemplate
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import java.util.*

@Repository
interface VersionRepository : org.springframework.data.repository.Repository<VersionEntity, UUID>,
    Neo4jRepository<VersionEntity, UUID> {
    fun findProjByVersionOfId(componentId: UUID): List<VersionProjection>
    fun findProjByVersionOfIdAndId(componentId: UUID, versionId: UUID): VersionProjection?
    fun findByVersionOfIdentifierAndIdentifier(componentIdentifier: String, versionIdentifier: String): VersionEntity?
    fun findByVersionOfIdentifier(componentIdentifier: String):  List<VersionEntity>
}

@Service
class VersionsDao(
    private val repository: VersionRepository,
    private val neo4jTemplate: Neo4jTemplate
) : Versions {
    override fun findVersionByComponentAndId(
        componentId: ComponentId,
        versionId: VersionId
    ): Version? {
        val projection = repository.findProjByVersionOfIdAndId(componentId.value, versionId.value)
        return projection?.toVersion()
    }

    override fun findAllByComponent(id: ComponentId): List<Version> {
        return repository.findProjByVersionOfId(id.value).map { it.toVersion() }
    }

    override fun save(version: Version): Version {
        val listenTo = mutableSetOf<IncomingInterfaceEntity>()
        val sendTo = mutableSetOf<OutgoingInterfaceEntity>()
        val entity = VersionEntity(
            version.id,
            version.identifier.value,
            ComponentEntity(
                version.versionOf.id,
                version.versionOf.version
            ),
            version.version
        ).apply {
            listeningTo = version.listeningTo.map {
                val receivedBy = incomingInterfaceEntity(
                    it.id,
                    it.version,
                    it.protocol,
                    it.protocolData
                )
                listenTo.firstOrNull { it.id == receivedBy.id } ?: receivedBy.also { listenTo.add(it) }
            }.toSet()
            sendingTo = version.sendingTo.map {
                val sentTo = outgoingInterfaceEntity(
                    it.id,
                    it.version,
                    it.protocol,
                    it.protocolData
                )
                sendTo.firstOrNull { it.id == sentTo.id } ?: sentTo.also { sendTo.add(it) }
            }.toSet()
            testedBy = version.testedBy.map { abstractTest ->
                abstractTestCaseEntity(abstractTest.id, abstractTest.identifier,
                    abstractTest.templateFor.map { concreteTest ->
                        val convertedMessages = mutableListOf<MessageEntity>()
                        val status = concreteTest.status.toString()
                        concreteTest.triggeredMessages.forEach { it ->
                            when (it) {
                                is StimulusMessage -> {
                                    stimulusEntity(
                                        it.id,
                                        it.version,
                                        it.value.toString(),
                                        it.order,
                                        listenTo.first { inter -> inter.id == it.receivedBy.id.value }
                                    ).also {
                                        convertedMessages += it
                                    }
                                }

                                is ComponentResponseMessage -> {
                                    componentResponseEntity(
                                        it.id,
                                        it.version,
                                        it.value.toString(),
                                        it.order,
                                        sendTo.first { inter -> inter.id == it.sentBy.id.value }
                                    ).apply {
                                        dependsOn =
                                            it.dependsOn.map { it.toEntity() }
                                                .toSortedSet()
                                    }.also {
                                        convertedMessages += it
                                    }
                                }

                                is EnvironmentResponseMessage -> {
                                    environmentResponseEntity(
                                        it.id,
                                        it.version,
                                        it.value.toString(),
                                        it.order,
                                        listenTo.first { inter -> inter.id == it.receivedBy.id.value }
                                    ).apply {
                                        reactionTo =
                                            it.reactionTo.toEntity()
                                    }.also {
                                        convertedMessages += it
                                    }
                                }
                            }
                        }

                        when (concreteTest) {
                            is UnitTest ->
                                unitTestEntity(
                                    concreteTest.id,
                                    concreteTest.version,
                                    concreteTest.identifier,
                                    concreteTest.parameters,
                                    convertedMessages.toSortedSet(),
                                    status
                                )
                            is InteractionTest ->
                                interactionTestEntity(
                                    concreteTest.id,
                                    concreteTest.version,
                                    concreteTest.identifier,
                                    concreteTest.parameters,
                                    convertedMessages.toSortedSet(),
                                    status
                                )
                        }
                    }.toSet(),
                    abstractTest.version
                )
            }.toSet()
        }
        val ent = neo4jTemplate.saveAs(entity, VersionProjection::class.java)
        return ent.toVersion()
    }
}