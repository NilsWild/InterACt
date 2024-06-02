package de.interact

import de.interact.repository.IncomingRestInterfaceProtocolData
import de.interact.repository.OutgoingRestInterfaceProtocolData
import org.springframework.stereotype.Service

@Service
class RestInterfaceMatcher {
    fun matches(out: OutgoingRestInterfaceProtocolData, incoming: IncomingRestInterfaceProtocolData): Boolean {
        return out.method == incoming.method && out.path == incoming.path && out.request == incoming.request
    }

    fun matches(incoming: IncomingRestInterfaceProtocolData, out: OutgoingRestInterfaceProtocolData): Boolean {
        return matches(out, incoming)
    }
}