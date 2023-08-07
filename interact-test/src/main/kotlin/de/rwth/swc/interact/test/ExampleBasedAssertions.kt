package de.rwth.swc.interact.test

import de.rwth.swc.interact.domain.TestMode

fun givenExpectedResults(assertion: () -> Unit) {
    try {
        assertion()
    } catch (e: AssertionError) {
        if (ExITConfiguration.mode == TestMode.UNIT) {
            throw ExampleBasedAssertionError(e.message)
        }
    }
}
