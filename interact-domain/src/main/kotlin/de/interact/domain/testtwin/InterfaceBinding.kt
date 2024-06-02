package de.interact.domain.testtwin

import de.interact.domain.shared.InterfaceBindingId
import de.interact.domain.testtwin.`interface`.IncomingInterface
import de.interact.domain.testtwin.`interface`.OutgoingInterface

data class InterfaceBinding(
    val id: InterfaceBindingId,
    val from: OutgoingInterface,
    val to: IncomingInterface,
    val createdBy: BinderIdentifier
)

@JvmInline
value class BinderIdentifier(val value: String) {
    override fun toString(): String {
        return value
    }
}

