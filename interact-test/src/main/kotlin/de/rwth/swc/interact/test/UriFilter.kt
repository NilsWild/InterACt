package de.rwth.swc.interact.test

import de.rwth.swc.interact.utils.Logging
import de.rwth.swc.interact.utils.logger
import java.net.URI

/**
 * Interface to filter URIs. This is used to filter the domain of urls.
 * E.g. the hosts of components and mocks can be aliased so the urls can be matched across test runs.
 */
interface UriFilter {

    fun filter(uri: URI): String

}

/**
 * Filters the URI to only contain the path and the query.
 * E.g. http://localhost:8080/api?param=value -> /api?param=value
 */
class StripUriFilter : UriFilter {
    override fun filter(uri: URI): String {
        return uri.let { _uri ->
            _uri.query?.let { query ->
                _uri.path + "?" + query
            } ?: _uri.path

        }
    }
}

/**
 * Filters the URI by aliasing the hosts domain.
 * E.g. http://localhost:8080/api?param=value -> http://myservice/api?param=value
 */
class AliasUriFilter(var mapping: Map<String, String>) : UriFilter, Logging {

    private val logger = logger()

    override fun filter(uri: URI): String {
        val uriString = uri.toString()
        val domain = getDomain(uriString)
        return mapping[domain]?.let {
            uriString.replaceFirst(domain, it)
        } ?: run {
            logger.warn("No mapping found for domain $domain")
            uriString
        }
    }

    private fun getDomain(uri: String): String {
        return uri.replace(Regex("http(s)?://|/.*"), "");
    }
}