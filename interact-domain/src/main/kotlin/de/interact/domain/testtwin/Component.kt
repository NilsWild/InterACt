package de.interact.domain.testtwin

import arrow.optics.optics
import de.interact.domain.shared.ComponentId
import de.interact.domain.shared.Entity

@optics
data class Component(
    override val id: ComponentId,
    val identifier: ComponentIdentifier,
    override val version: Long? = null
): Entity<ComponentId>(){
    companion object
}

@JvmInline
value class ComponentIdentifier(val value: String) {
    override fun toString(): String {
        return value
    }
}

