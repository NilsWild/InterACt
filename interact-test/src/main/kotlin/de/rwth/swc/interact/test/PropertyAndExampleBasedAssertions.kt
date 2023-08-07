package de.rwth.swc.interact.test

import de.rwth.swc.interact.domain.TestMode

fun inherently(assertion: ()-> Unit) {
    try {
        assertion()
    } catch (e: AssertionError) {
        throw PropertyBasedAssertionError(e.message)
    }
}

fun givenExpectedResults(assertion: ()-> Unit) {
    try {
        assertion()
    } catch (e: AssertionError) {
        if(ExITConfiguration.mode == TestMode.UNIT) {
            throw ExampleBasedAssertionError(e.message)
        }
    }
}

class PropertyBasedAssertionError(message: String?): AssertionError(message)
class ExampleBasedAssertionError(message: String?): AssertionError(message)
