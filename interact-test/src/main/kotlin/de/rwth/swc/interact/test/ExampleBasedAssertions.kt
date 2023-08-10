package de.rwth.swc.interact.test

fun forExample(assertion: () -> Unit) {
    try {
        assertion()
    } catch (e: AssertionError) {
        throw ExampleBasedAssertionError(e.message)
    }
}
