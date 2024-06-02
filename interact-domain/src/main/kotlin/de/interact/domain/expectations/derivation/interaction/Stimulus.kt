package de.interact.domain.expectations.derivation.interaction

import de.interact.domain.shared.ComponentResponseMessageId
import de.interact.domain.shared.EntityReference
import de.interact.domain.shared.OutgoingInterfaceId

data class Stimulus (
    val message: EntityReference<ComponentResponseMessageId>,
    val interfaceId: EntityReference<OutgoingInterfaceId>
)