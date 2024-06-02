package de.interact.test

fun inherently(assertion: () -> Unit) {
    try {
        assertion()
    } catch (e: AssertionError) {
        throw PropertyBasedAssertionError(e.message)
    }
}

