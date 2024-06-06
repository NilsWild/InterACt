package de.interact.rest.observer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.net.URI

internal class PathVariableExtractorTest {

    @Test
    fun `can extract path variable from url without host`() {
        val template = "/books/{id}"
        val url = URI.create("/books/1?p=1")
        val result = PathVariableExtractor.extractPathVariablesFromUrl(template, url)
        assertNotNull(result)
        assertEquals("1", result!!.uriVariables["id"])
    }

    @Test
    fun `can extract path variable from url with host`() {
        val template = "/books/{id}"
        val url = URI.create("http://localhost:80801/books/1")
        val result = PathVariableExtractor.extractPathVariablesFromUrl(template, url)
        assertNotNull(result)
        assertEquals("1", result!!.uriVariables["id"])
    }
}