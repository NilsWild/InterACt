package de.interact.junit.jupiter.systemexpectations

/** class SystemExpectationInvocationContext(
    private val systemExpectation: SystemExpectation,
    private val phase: SystemExpectationPhase
) : TestTemplateInvocationContext {
    override fun getAdditionalExtensions(): List<Extension> {
        return listOf(
            StimulusAndResponseParameterResolver(systemExpectation),
            SystemExpectationExtension(systemExpectation, phase)
        )
    }
}

class StimulusAndResponseParameterResolver(private val expectation: SystemExpectation) :
    ParameterResolver {

    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        return true
    }

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any? {
        return when (parameterContext.index) {
            0 -> expectation
            1 -> ParameterTypeResolver.resolveArgumentToParameterType(parameterContext, expectation.stimulus)
            2 -> ParameterTypeResolver.resolveArgumentToParameterType(parameterContext, expectation.reaction)
            else -> null
        }
    }
}

class SystemExpectationExtension(
    private val systemExpectation: SystemExpectation,
    private val phase: SystemExpectationPhase
) : InvocationInterceptor {

    override fun interceptTestTemplateMethod(
        invocation: InvocationInterceptor.Invocation<Void>,
        invocationContext: ReflectiveInvocationContext<Method>,
        extensionContext: ExtensionContext
    ) {
        try {
            super.interceptTestTemplateMethod(invocation, invocationContext, extensionContext)
            systemExpectation.execute()
            systemExpectation.status = TestState.TestFinishedState.Succeeded
        } catch (e: SystemPropertyMessageFilterError) {
            if (phase == SystemExpectationPhase.STIMULUS_FILTERING || phase == SystemExpectationPhase.ASSERTION) {
                systemExpectation.status = TestState.TestFinishedState.Failed.AssertionFailed
                throw e
            }
        } catch (e: SystemPropertyAssertionError) {
            if (phase == SystemExpectationPhase.ASSERTION) {
                systemExpectation.status = TestState.TestFinishedState.Failed.AssertionFailed
                throw e
            }
        } catch (e: Throwable) {
            systemExpectation.status = TestState.TestFinishedState.Failed.ExceptionFailed
            throw e
        }
    }
}
**/