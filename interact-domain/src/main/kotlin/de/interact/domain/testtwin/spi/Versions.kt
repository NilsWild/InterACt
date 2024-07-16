package de.interact.domain.testtwin.spi

import de.interact.domain.shared.ComponentId
import de.interact.domain.shared.VersionId
import de.interact.domain.testtwin.ComponentIdentifier
import de.interact.domain.testtwin.Version
import de.interact.domain.testtwin.VersionIdentifier
import kotlin.reflect.KFunction

interface Versions {

    fun findVersionByComponentAndId(componentId: ComponentId, versionId: VersionId): Version?
    fun findAllByComponent(id: ComponentId): List<Version>
    infix fun save(version: Version): Version

    infix fun `for component`(id: ComponentId): ComponentVersionOperations = { function, arguments ->
        function.call(this, id, *arguments)
    }
}

typealias ComponentVersionOperations = (KFunction<*>, Array<Any>) -> Any?

infix fun ComponentVersionOperations.`find by id`(id: VersionId): Version? {
    return this.invoke(Versions::findVersionByComponentAndId, arrayOf(id)) as Version?
}

@Suppress("UNCHECKED_CAST")
fun ComponentVersionOperations.all(): List<Version> {
    return this.invoke(Versions::findAllByComponent, emptyArray()) as List<Version>
}