package de.rwth.swc.interact.utbi

/**
 * Interface to be implemented by interface binders to bind interfaces of components to one another
 * in the unit test based integration (utbi) model.
 */
interface InterfaceBinder {

    val name: InterfaceBinderName

    val version: InterfaceBinderVersion

    fun bindInterfaces()

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
