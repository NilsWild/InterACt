package de.rwth.swc.interact.utbi

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class TopicExchangeMatcherTest {

    @ParameterizedTest
    @CsvSource(
        "quick.orange.rabbit,quick.orange.rabbit",
        "quick.orange.rabbit,*.orange.*",
        "quick.orange.rabbit,*.*.rabbit",
        "lazy.brown.elephant,lazy.#"
    )
    fun `match topic exchange`(routingKey: String, topicBinding: String) {
        val matcher = TopicExchangeMatcher()
        val actual = matcher.match(routingKey, topicBinding)
        assertThat(actual).isTrue()
    }

    @ParameterizedTest
    @CsvSource(
        "quick.orange.fox,quick.orange.*.fox",
        "lazy.brown.fox,*.fox",
        "lazy.pink.rabbit,quick.#"
    )
    fun `not match topic exchange`(routingKey: String, topicBinding: String) {
        val matcher = TopicExchangeMatcher()
        val actual = matcher.match(routingKey, topicBinding)
        assertThat(actual).isFalse()
    }
}