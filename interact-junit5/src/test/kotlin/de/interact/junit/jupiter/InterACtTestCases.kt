package de.interact.junit.jupiter

import de.interact.junit.jupiter.annotation.InterACtTest
import de.interact.test.forExample
import de.interact.test.inherently
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.params.aggregator.AggregateWith
import org.junit.jupiter.params.aggregator.ArgumentsAccessor
import org.junit.jupiter.params.aggregator.ArgumentsAggregator
import org.junit.jupiter.params.provider.CsvSource

internal class InterACtTestCases {

    @InterACtTest
    @CsvSource(
        value = [
            "Test, true"
        ]
    )
    fun `test with expected result for unit test`(name: String, expectedResult: Boolean?) {
        forExample {
            assertThat(expectedResult).isNotNull()
        }
        inherently {
            assertThat(name).isNotEmpty()
        }
    }

    @InterACtTest
    @CsvSource(
        value = [
            "Test, Test"
        ]
    )
    fun `test with aggregator`(@AggregateWith(TestObjAggregator::class) testobj: TestObj) {
        forExample {
            assertThat(testobj.name).isEqualTo(testobj.name2)
        }
        inherently {
            assertThat(testobj.name).isNotEmpty
            assertThat(testobj.name2).isNotEmpty
        }
    }

    data class TestObj(val name: String, val name2: String)

    class TestObjAggregator : ArgumentsAggregator {
        override fun aggregateArguments(arguments: ArgumentsAccessor, context: ParameterContext): Any {
            return TestObj(arguments.getString(0), arguments.getString(1))
        }
    }
}