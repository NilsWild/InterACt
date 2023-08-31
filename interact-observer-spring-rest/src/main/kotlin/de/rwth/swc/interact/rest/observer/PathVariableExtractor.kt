package de.rwth.swc.interact.rest.observer

import org.springframework.http.server.PathContainer
import org.springframework.web.util.pattern.PathPattern
import org.springframework.web.util.pattern.PathPatternParser
import java.net.URI

object PathVariableExtractor {

    fun extractPathVariablesFromUrl(template: String, url: URI): PathPattern.PathMatchInfo? {
        val parser = PathPatternParser()
        val pathPattern = parser.parse(template)
        val container = PathContainer.parsePath(url.path)
        return pathPattern.matchAndExtract(container)
    }

}