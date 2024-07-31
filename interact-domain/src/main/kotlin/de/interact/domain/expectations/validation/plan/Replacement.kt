package de.interact.domain.expectations.validation.plan

import de.interact.domain.shared.Entity
import de.interact.domain.shared.ReplacementId
import java.util.*

data class Replacement(
    val messageToReplace: MessageToReplaceIdentifier,
    val replacement: ReplacementIdentifier,
    override val id: ReplacementId = ReplacementId(UUID.randomUUID()),
    override val version: Long? = null
): Entity<ReplacementId>() {
    fun clone(): Replacement {
        return this.copy(id = ReplacementId(UUID.randomUUID()))
    }
}