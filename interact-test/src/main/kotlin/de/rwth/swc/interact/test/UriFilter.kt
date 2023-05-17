package de.rwth.swc.interact.test

import java.net.URI

interface UriFilter {

    fun filter(uri: String): String

}

class StripUriFilter : UriFilter {
    override fun filter(uri: String): String {
        return URI(uri).let { _uri ->
            _uri.query?.let { query ->
                _uri.path + "?" + query
            } ?: _uri.path

        }
    }
}

class AliasUriFilter(var mapping: Map<String, String>) : UriFilter {

    override fun filter(uri: String): String {
        val domain = getDomain(uri)
        return mapping[domain]?.let {
            uri.replaceFirst(domain, it)
        } ?: uri
    }

    private fun getDomain(uri: String): String {
        return uri.replace(Regex("http(s)?://|/.*"), "");
    }
}