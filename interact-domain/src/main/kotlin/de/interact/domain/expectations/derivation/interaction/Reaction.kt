package de.interact.domain.expectations.derivation.interaction

import de.interact.domain.shared.EntityReference
import de.interact.domain.shared.EnvironmentResponseMessageId
import de.interact.domain.shared.IncomingInterfaceId

data class Reaction(
    val message: EntityReference<EnvironmentResponseMessageId>,
    val interfaceId: EntityReference<IncomingInterfaceId>
)