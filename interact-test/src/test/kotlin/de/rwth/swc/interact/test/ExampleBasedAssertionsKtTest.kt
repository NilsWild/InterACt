package de.rwth.swc.interact.test

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class ExampleBasedAssertionsKtTest {

    @Test
    fun `inherently should throw PropertyBasedAssertionError when assertion fails`() {
        val assertion = {
            forExample {
                Assertions.assertThat(true).withFailMessage("message").isEqualTo(false)
            }
        }
        val expected = ExampleBasedAssertionError("message")
        assertThatThrownBy(assertion)
            .isInstanceOf(expected::class.java)
            .hasMessage(expected.message)
    }
}