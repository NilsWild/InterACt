package de.rwth.swc.interact.utbi

import de.rwth.swc.interact.domain.ComponentInterface
import de.rwth.swc.interact.domain.IncomingInterface
import de.rwth.swc.interact.domain.OutgoingInterface

/**
 * Interface to be implemented by interface binders to bind interfaces of components to one another
 * in the unit test based integration (utbi) model.
 */
interface InterfaceBinder {

    val name: InterfaceBinderName

    val version: InterfaceBinderVersion

    fun bindInterfaces(componentInterface: ComponentInterface)
    fun canHandle(componentInterface: ComponentInterface) : Boolean

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
