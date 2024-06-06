package de.interact

import de.interact.domain.rest.*
import org.springframework.http.server.PathContainer
import org.springframework.stereotype.Service
import org.springframework.web.util.pattern.PathPatternParser

@Service
class RestInterfaceMatcher(private val pathPatternParser: PathPatternParser) {
    fun matches(outgoing: OutgoingRestInterface, incoming: IncomingRestInterface): Boolean {
        return matches(incoming.protocolData, outgoing.protocolData)
    }

    fun matches(incoming: IncomingRestInterface, outgoing: OutgoingRestInterface): Boolean {
        return matches(incoming.protocolData, outgoing.protocolData)
    }

    fun matches(incoming: IncomingRestInterface, expectation: IncomingRestInterfaceExpectation): Boolean {
        return matches(incoming.protocolData, expectation.protocolData)
    }

    fun matches(outgoing: OutgoingRestInterface, expectation: OutgoingRestInterfaceExpectation): Boolean {
        return matches(expectation.protocolData, outgoing.protocolData)
    }

    private fun matches(incoming: RestInterfaceData, outgoing: RestInterfaceData): Boolean {
        return outgoing.method == incoming.method &&
                outgoing.request == incoming.request &&
                if(outgoing.request) {
                    pathPatternParser.parse(incoming.path).matches(PathContainer.parsePath(outgoing.path))
                } else {
                    pathPatternParser.parse(outgoing.path).matches(PathContainer.parsePath(incoming.path))
                }
    }
}