package de.rwth.swc.interact.junit.jupiter

import de.rwth.swc.interact.domain.TestInvocationDescriptor
import de.rwth.swc.interact.domain.TestMode
import org.junit.jupiter.api.extension.Extension
import org.junit.jupiter.api.extension.TestTemplateInvocationContext


class InterACtTestInvocationContext(
    private val formatter: InterACtTestNameFormatter,
    private val methodContext: InterACtTestMethodContext,
    private val arguments: Array<Any?>,
    private val invocationIndex: Int,
    private val mode: TestMode,
    private val testInvocationDescriptor: TestInvocationDescriptor? = null
) : TestTemplateInvocationContext {

    override fun getDisplayName(invocationIndex: Int): String {
        return formatter.format(arguments)
    }

    override fun getAdditionalExtensions(): List<Extension> {
        return listOf(
            InterACtTestParameterResolver(methodContext, arguments, invocationIndex, mode),
            InterACtTestInvocationInterceptor(mode, testInvocationDescriptor),
            StoreObservationsExtension()
        )
    }
}