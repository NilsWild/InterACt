package de.interact.domain.testtwin

import arrow.optics.optics
import de.interact.domain.shared.ComponentId
import de.interact.domain.shared.Entity
import de.interact.domain.shared.EntityReference
import de.interact.domain.shared.VersionId
import de.interact.domain.testtwin.abstracttest.AbstractTestCase
import de.interact.domain.testtwin.`interface`.IncomingInterface
import de.interact.domain.testtwin.`interface`.OutgoingInterface
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

    companion object {}

    fun mergeWithVersionInfo(version: Version): Version {
        if (version.id != id) {
            throw IllegalArgumentException("Cannot merge data of different versions")
        }
        mergeConcreteTestCasesForExistingAbstractTestCases(version)
        return this.copy(
            testedBy = testedBy + version.testedBy,
            listeningTo = listeningTo + version.listeningTo,
            sendingTo = sendingTo + version.sendingTo
        )
    }

    private fun mergeConcreteTestCasesForExistingAbstractTestCases(version: Version) {
        val originalTestMap = testedBy.associateBy { it.id }
        val addedTestMap = version.testedBy.associateBy { it.id }
        originalTestMap.keys.intersect(addedTestMap.keys).forEach {
            originalTestMap[it]!!.templateFor += addedTestMap[it]!!.templateFor
        }
    }
}

@JvmInline
value class VersionIdentifier(val value: String) {
    override fun toString(): String {
        return value
    }
}
