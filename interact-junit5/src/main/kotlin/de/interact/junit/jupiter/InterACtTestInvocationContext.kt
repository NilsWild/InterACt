package de.interact.junit.jupiter

import org.junit.jupiter.api.extension.Extension
import org.junit.jupiter.api.extension.TestTemplateInvocationContext


class InterACtTestInvocationContext(
    private val formatter: InterACtTestNameFormatter,
    private val methodContext: InterACtTestMethodContext,
    private val arguments: Array<Any?>,
    private val invocationIndex: Int,
    private val mode: TestMode
) : TestTemplateInvocationContext {

    override fun getDisplayName(invocationIndex: Int): String {
        return formatter.format(arguments)
    }

    override fun getAdditionalExtensions(): List<Extension> {
        return listOf(
            InterACtTestParameterResolver(methodContext, arguments, invocationIndex, mode),
            InterACtTestInvocationInterceptor(mode)
        )
    }
}