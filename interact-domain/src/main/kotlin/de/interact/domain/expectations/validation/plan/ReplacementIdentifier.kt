package de.interact.domain.expectations.validation.plan

import de.interact.domain.shared.EntityReference
import de.interact.domain.shared.OutgoingInterfaceId

data class ReplacementIdentifier(
    val interfaceToCopyFrom: EntityReference<OutgoingInterfaceId>
)