package de.interact.utbi

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@Suppress("UNCHECKED_CAST")
internal class HeaderExchangeMatcherTest {

    @ParameterizedTest
    @CsvSource(
        "null,1,null,1,null",
        "any,1,1,1,null",
        "any,1,1,null,1",
        "all,1,1,1,1"
    )
    fun `match header exchange`(
        xMatch: String?,
        argument1: String?,
        argument2: String?,
        header1: String?,
        header2: String?
    ) {
        val matcher = HeaderExchangeMatcher()
        val actual = matcher.match(
            mapOf(
                "argument1" to header1,
                "argument2" to header2
            ).filterValues { it != null } as Map<String, String>,
            mapOf(
                "x-match" to xMatch,
                "argument1" to argument1,
                "argument2" to argument2
            ).filterValues { it != null } as Map<String, String>
        )
        assertThat(actual).isTrue()
    }

    @ParameterizedTest
    @CsvSource(
        "null,1,null,null,1",
        "any,1,1,2,null",
        "all,1,1,1,null"
    )
    fun `not match header exchange`(
        xMatch: String?,
        argument1: String?,
        argument2: String?,
        header1: String?,
        header2: String?
    ) {
        val matcher = HeaderExchangeMatcher()
        val actual = matcher.match(
            mapOf(
                "argument1" to header1,
                "argument2" to header2
            ).filterValues { it != null } as Map<String, String>,
            mapOf(
                "x-match" to xMatch,
                "argument1" to argument1,
                "argument2" to argument2
            ).filterValues { it != null } as Map<String, String>
        )
        assertThat(actual).isFalse()
    }

}