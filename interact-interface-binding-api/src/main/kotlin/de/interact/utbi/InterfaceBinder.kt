package de.interact.utbi

import de.interact.domain.testtwin.api.event.InterfaceAddedToVersionEvent

/**
 * Interface to be implemented by interface binders to bind interfaces of components to one another
 * in the unit test based integration (utbi) model.
 */
interface InterfaceBinder {

    val name: InterfaceBinderName

    val version: InterfaceBinderVersion

    fun bindInterfaces(interfaceAddedEvent: InterfaceAddedToVersionEvent)
    fun canHandle(interfaceAddedEvent: InterfaceAddedToVersionEvent): Boolean

}

@JvmInline
value class InterfaceBinderName(val name: String) {
    override fun toString(): String {
        return name
    }
}

@JvmInline
value class InterfaceBinderVersion(val version: String) {
    override fun toString(): String {
        return version
    }
}
