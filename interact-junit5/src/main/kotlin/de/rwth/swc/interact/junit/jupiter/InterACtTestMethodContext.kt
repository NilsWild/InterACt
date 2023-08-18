package de.rwth.swc.interact.junit.jupiter

import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolutionException
import org.junit.jupiter.params.aggregator.AggregateWith
import org.junit.jupiter.params.aggregator.ArgumentsAccessor
import org.junit.jupiter.params.aggregator.ArgumentsAggregator
import org.junit.jupiter.params.aggregator.DefaultArgumentsAccessor
import org.junit.jupiter.params.converter.ArgumentConverter
import org.junit.jupiter.params.converter.ConvertWith
import org.junit.jupiter.params.converter.DefaultArgumentConverter
import org.junit.jupiter.params.support.AnnotationConsumerInitializer
import org.junit.platform.commons.util.AnnotationUtils
import org.junit.platform.commons.util.AnnotationUtils.isAnnotated
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import kotlin.reflect.full.createInstance


class InterACtTestMethodContext(testMethod: Method) {

    private val parameters: Array<Parameter>
    private val resolvers: Array<Resolver?>
    private val resolverTypes: MutableList<ResolverType> = mutableListOf()

    init {
        this.parameters = testMethod.parameters
        this.resolvers = arrayOfNulls(this.parameters.size)
        for (parameter in this.parameters) {
            if (isArgumentsAccessor(parameter)) {
                throw ParameterResolutionException(
                    "ArgumentAccessors are not supported. Use @AggregateWith instead. [" + parameter.type.name +
                            "] is not supported for @InterACtTest method [" + testMethod.name + "]."
                )
            }
            if (isAggregator(parameter)) {
                this.resolverTypes.add(ResolverType.AGGREGATOR)
            } else {
                this.resolverTypes.add(ResolverType.CONVERTER)
            }
        }
    }

    /**
     * Determine if the supplied [Parameter] is an [ArgumentsAccessor]).
     *
     * @return `true` if the parameter is an aggregator
     */
    private fun isArgumentsAccessor(parameter: Parameter): Boolean {
        return ArgumentsAccessor::class.java.isAssignableFrom(parameter.type)
    }

    /**
     * Determine if the supplied [Parameter] is annotated with [AggregateWith]).
     *
     * @return `true` if the parameter is an aggregator
     */
    private fun isAggregator(parameter: Parameter): Boolean {
        return isAnnotated(parameter, AggregateWith::class.java)
    }

    /**
     * Determine if the [Method] represented by this context has a
     * *potentially* valid signature (i.e., formal parameter
     * declarations) with regard to aggregators.
     *
     *
     * This method takes a best-effort approach at enforcing the following
     * policy for parameterized test methods that accept aggregators as arguments.
     *
     *
     *  1. zero or more *indexed arguments* come first.
     *  1. zero or more *aggregators* come next.
     *  1. zero or more arguments supplied by other `ParameterResolver`
     * implementations come last.
     *
     *
     * @return `true` if the method has a potentially valid signature
     */
    fun hasPotentiallyValidSignature(): Boolean {
        var indexOfPreviousAggregator = -1
        for (i in 0 until getParameterCount()) {
            if (isAggregator(i)) {
                if ((indexOfPreviousAggregator != -1) && (i != indexOfPreviousAggregator + 1)) {
                    return false
                }
                indexOfPreviousAggregator = i
            }
        }
        return true
    }

    /**
     * Get the number of parameters of the [Method] represented by this
     * context.
     */
    fun getParameterCount(): Int {
        return parameters.size
    }

    /**
     * Get the name of the [Parameter] with the supplied index, if
     * it is present and declared before the aggregators.
     *
     * @return an `Optional` containing the name of the parameter
     */
    fun getParameterName(parameterIndex: Int): String? {
        if (parameterIndex >= getParameterCount()) {
            return null
        }
        val parameter: Parameter = parameters[parameterIndex]
        if (!parameter.isNamePresent) {
            return null
        }
        return if (hasAggregator() && parameterIndex >= indexOfFirstAggregator()) {
            null
        } else {
            parameter.name
        }
    }

    /**
     * Determine if the [Method] represented by this context declares at
     * least one [Parameter] that is an
     * [aggregator][.isAggregator].
     *
     * @return `true` if the method has an aggregator
     */
    fun hasAggregator(): Boolean {
        return resolverTypes.contains(ResolverType.AGGREGATOR)
    }

    /**
     * Determine if the [Parameter] with the supplied index is an
     * aggregator (i.e., of type [ArgumentsAccessor] or annotated with
     * [AggregateWith]).
     *
     * @return `true` if the parameter is an aggregator
     */
    fun isAggregator(parameterIndex: Int): Boolean {
        return resolverTypes[parameterIndex] === ResolverType.AGGREGATOR
    }

    /**
     * Find the index of the first [aggregator][.isAggregator]
     * [Parameter] in the [Method] represented by this context.
     *
     * @return the index of the first aggregator, or `-1` if not found
     */
    fun indexOfFirstAggregator(): Int {
        return resolverTypes.indexOf(ResolverType.AGGREGATOR)
    }

    /**
     * Resolve the parameter for the supplied context using the supplied
     * arguments.
     */
    fun resolve(parameterContext: ParameterContext, arguments: Array<Any?>, invocationIndex: Int): Any? {
        return getResolver(parameterContext).resolve(parameterContext, arguments, invocationIndex)
    }

    private fun getResolver(parameterContext: ParameterContext): Resolver {
        val index = parameterContext.index
        if (resolvers.getOrNull(index) == null) {
            resolvers[index] = resolverTypes[index].createResolver(parameterContext)
        }
        return resolvers[index]!!
    }

    internal sealed class ResolverType {
        object CONVERTER : ResolverType() {
            override fun createResolver(parameterContext: ParameterContext): Resolver {
                return try { // @formatter:off
                    AnnotationUtils.findAnnotation(parameterContext.parameter, ConvertWith::class.java)
                        .map(ConvertWith::value)
                        .map { clazz -> clazz.createInstance() as ArgumentConverter }
                        .map { converter ->
                            AnnotationConsumerInitializer.initialize(
                                parameterContext.parameter,
                                converter
                            )
                        }
                        .map { argumentConverter: ArgumentConverter -> Converter(argumentConverter) }
                        .orElse(Converter.DEFAULT)
                } // @formatter:on
                catch (ex: Exception) {
                    throw parameterResolutionException("Error creating ArgumentConverter", ex, parameterContext)
                }
            }
        }

        object AGGREGATOR : ResolverType() {
            override fun createResolver(parameterContext: ParameterContext): Resolver {
                return try { // @formatter:off
                    AnnotationUtils.findAnnotation(parameterContext.parameter, AggregateWith::class.java)
                        .map { obj: AggregateWith -> obj.value }
                        .map { clazz -> clazz.createInstance() }
                        .map { argumentsAggregator: ArgumentsAggregator -> Aggregator(argumentsAggregator) }
                        .orElse(Aggregator.DEFAULT)
                } // @formatter:on
                catch (ex: Exception) {
                    throw parameterResolutionException("Error creating ArgumentsAggregator", ex, parameterContext)
                }
            }
        }

        abstract fun createResolver(parameterContext: ParameterContext): Resolver
    }

    internal interface Resolver {
        fun resolve(parameterContext: ParameterContext, arguments: Array<Any?>, invocationIndex: Int): Any?
    }

    internal class Converter(private val argumentConverter: ArgumentConverter) : Resolver {

        override fun resolve(parameterContext: ParameterContext, arguments: Array<Any?>, invocationIndex: Int): Any? {
            val argument = arguments[parameterContext.index]
            return try {
                argumentConverter.convert(argument, parameterContext)
            } catch (ex: Exception) {
                throw parameterResolutionException("Error converting parameter", ex, parameterContext)
            }
        }

        companion object {
            internal val DEFAULT = Converter(DefaultArgumentConverter.INSTANCE)
        }
    }

    internal class Aggregator(private val argumentsAggregator: ArgumentsAggregator) : Resolver {
        override fun resolve(parameterContext: ParameterContext, arguments: Array<Any?>, invocationIndex: Int): Any? {
            val args = arguments.copyOfRange(parameterContext.index, arguments.size)
            val accessor = DefaultArgumentsAccessor(parameterContext, invocationIndex, *args)
            return try {
                argumentsAggregator.aggregateArguments(accessor, parameterContext)
            } catch (ex: Exception) {
                throw parameterResolutionException("Error aggregating arguments for parameter", ex, parameterContext)
            }
        }

        companion object {
            internal val DEFAULT = Aggregator { accessor: ArgumentsAccessor, _: ParameterContext -> accessor }
        }
    }

    companion object {
        private fun parameterResolutionException(
            message: String,
            cause: Exception,
            parameterContext: ParameterContext
        ): ParameterResolutionException {
            var fullMessage = message + " at index " + parameterContext.index
            if (cause.message?.isNotBlank() == true) {
                fullMessage += ": " + cause.message
            }
            return ParameterResolutionException(fullMessage, cause)
        }
    }
}