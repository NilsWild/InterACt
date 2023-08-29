package de.rwth.swc.interact.junit.jupiter

import de.rwth.swc.interact.junit.jupiter.annotation.InterACtTest
import de.rwth.swc.interact.test.forExample
import de.rwth.swc.interact.test.inherently
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
            "Test, true",
            "Test2, false"
        ]
    )
    fun `filled with null parameters in interaction test`(name: String, expectedResult: Boolean?) {
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
            "Test, Test",
            "Test2, Test2"
        ]
    )
    fun `works with aggregators`(@AggregateWith(TestObjAggregator::class) testobj: TestObj) {
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