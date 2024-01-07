package de.rwth.swc.interact.junit.jupiter

import de.rwth.swc.interact.domain.serialization.SerializationConstants
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.params.converter.DefaultArgumentConverter
import java.lang.reflect.ParameterizedType

object ParameterTypeResolution {

    fun resolveArgumentToParameterType(parameterContext: ParameterContext, argument: Any?): Any? {
        return if (argument == null) {
            null
        } else if (parameterContext.parameter.type == argument.javaClass) {
            argument
        } else if (parameterContext.parameter.type.isPrimitive) {
            DefaultArgumentConverter.INSTANCE.convert(argument, parameterContext)
        } else {
            val type = parameterContext.parameter.parameterizedType
            if (type is ParameterizedType) {
                val genericTypes = type.actualTypeArguments.map { SerializationConstants.mapper.typeFactory.constructType(it) }.toTypedArray()
                SerializationConstants.mapper.readValue(
                    argument.toString(),
                    SerializationConstants.mapper.typeFactory.constructParametricType(parameterContext.parameter.type, *genericTypes)
                )
            } else {
                SerializationConstants.mapper.readValue(argument.toString(), parameterContext.parameter.type)
            }
        }
    }
}
