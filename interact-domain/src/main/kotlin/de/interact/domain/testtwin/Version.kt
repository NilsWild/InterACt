package de.interact.domain.testtwin

import arrow.optics.optics
import de.interact.domain.shared.*
import de.interact.domain.testtwin.abstracttest.AbstractTestCase
import de.interact.domain.testtwin.abstracttest.concretetest.ConcreteTestCase
import de.interact.domain.testtwin.abstracttest.concretetest.message.*
import de.interact.domain.testtwin.abstracttest.concretetest.message.Message
import de.interact.domain.testtwin.abstracttest.concretetest.triggeredMessages
import de.interact.domain.testtwin.abstracttest.templateFor
import de.interact.domain.testtwin.componentinterface.IncomingInterface
import de.interact.domain.testtwin.componentinterface.OutgoingInterface
import java.util.*

@optics
data class Version(
    val versionOf: EntityReference<ComponentId>,
    val identifier: VersionIdentifier,
    val testedBy: Set<AbstractTestCase> = setOf(),
    val listeningTo: Set<IncomingInterface> = setOf(),
    val sendingTo: Set<OutgoingInterface> = setOf(),
    override val id: VersionId = VersionId(UUID.randomUUID()),
    override val version: Long? = null
): Entity<VersionId>(){

    companion object

    fun mergeWithVersionInfo(version: Version): Version {
        if (version.id != id) {
            throw IllegalArgumentException("Cannot merge data of different versions")
        }
        var mergedVersion = version
        val lens = Version.testedBy compose MyEvery.set() compose AbstractTestCase.templateFor compose MyEvery.set() compose ConcreteTestCase.triggeredMessages compose MyEvery.sortedSet() compose Message.receivedMessage compose Message.ReceivedMessage.receivedBy
        val lens2 = Version.testedBy compose MyEvery.set() compose AbstractTestCase.templateFor compose MyEvery.set() compose ConcreteTestCase.triggeredMessages compose MyEvery.sortedSet() compose Message.sentMessage compose Message.SentMessage.sentBy
        mergedVersion = lens.modify(mergedVersion) { inter ->  inter.copy(version = listeningTo.firstOrNull{it.id == inter.id}?.version) }
        mergedVersion = lens2.modify(mergedVersion) { inter ->  inter.copy(version = sendingTo.firstOrNull{it.id == inter.id}?.version) }

        return this.copy(
            listeningTo = (listeningTo + version.listeningTo).distinctBy { it.id }.toSet(),
            sendingTo = (sendingTo + version.sendingTo).distinctBy { it.id }.toSet(),
            testedBy = mergeConcreteTestCasesForExistingAbstractTestCases(mergedVersion.testedBy)
        )
    }

    private fun mergeConcreteTestCasesForExistingAbstractTestCases(observedAbstractTestCases: Set<AbstractTestCase>): Set<AbstractTestCase> {
        val originalTestMap = testedBy.associateBy { it.id }
        val addedTestMap = observedAbstractTestCases.associateBy { it.id }
        val newTestedBy = originalTestMap.values.toMutableSet()

        originalTestMap.minus(addedTestMap.keys).forEach {
            newTestedBy += it.value
        }
        originalTestMap.keys.intersect(addedTestMap.keys).forEach {
            newTestedBy += originalTestMap[it]!!.copy(templateFor = (originalTestMap[it]!!.templateFor + addedTestMap[it]!!.templateFor).distinctBy { it.id }.toSet())
        }

        addedTestMap.minus(originalTestMap.keys).forEach {
            newTestedBy += it.value
        }
        return newTestedBy
    }
}

@JvmInline
value class VersionIdentifier(val value: String) {
    override fun toString(): String {
        return value
    }
}
