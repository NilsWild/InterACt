package de.interact.domain.expectations.derivation.interaction

data class Interaction(
    val stimulus: Stimulus,
    val reactions: Set<Reaction>
)