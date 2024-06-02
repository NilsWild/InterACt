package de.interact.domain.shared

@JvmInline
value class Protocol(val value: String) {
    override fun toString() = value
}


data class ProtocolData(val data: Map<String, String>) {
    override fun toString(): String {
        return data.toString()
    }
}