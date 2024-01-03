package de.rwth.swc.interact.junit.jupiter

import de.rwth.swc.interact.domain.TestResult
import de.rwth.swc.interact.observer.TestObserver
import org.junit.jupiter.api.extension.AfterTestExecutionCallback
import org.junit.jupiter.api.extension.ExtensionContext


/**
 * The InterACt junit extension is used to store the recorded observed messages
 */
class StoreObservationsExtension : AfterTestExecutionCallback {

    override fun afterTestExecution(context: ExtensionContext) {
        TestObserver.setTestResult(if (context.executionException.isEmpty) TestResult.SUCCESS else TestResult.FAILED)
        if (TestObserver.beforeStoringLatch?.isReleased() != false)
            TestObserver.pushObservations()
    }

}