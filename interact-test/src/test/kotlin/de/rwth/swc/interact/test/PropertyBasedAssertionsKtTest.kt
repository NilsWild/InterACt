package de.rwth.swc.interact.test

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class PropertyBasedAssertionsKtTest {

    @Test
    fun `inherently should throw PropertyBasedAssertionError when assertion fails`() {
        val assertion = {
            inherently {
                assertThat(true).withFailMessage("message").isEqualTo(false)
            }
        }
        val expected = PropertyBasedAssertionError("message")
        assertThatThrownBy(assertion)
            .isInstanceOf(expected::class.java)
            .hasMessage(expected.message)
    }
}