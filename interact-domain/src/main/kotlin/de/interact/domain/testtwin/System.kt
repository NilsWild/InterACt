package de.interact.domain.testtwin

data class System(
    val identifier: SystemIdentifier,
    val components: List<ComponentIdentifier>,
)

@JvmInline
value class SystemIdentifier(val value: String) {
    override fun toString(): String {
        return value
    }
}