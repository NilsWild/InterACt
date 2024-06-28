package de.interact.junit.jupiter

import de.interact.domain.serialization.SerializationConstants
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.params.converter.DefaultArgumentConverter
import java.lang.reflect.ParameterizedType

object ParameterTypeResolver {

    @JvmStatic
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
                val genericTypes =
                    type.actualTypeArguments.map { SerializationConstants.messageMapper.typeFactory.constructType(it) }
                        .toTypedArray()
                SerializationConstants.messageMapper.readValue(
                    argument.toString(),
                    SerializationConstants.messageMapper.typeFactory.constructParametricType(
                        parameterContext.parameter.type,
                        *genericTypes
                    )
                )
            } else {
                SerializationConstants.messageMapper.readValue(argument.toString(), parameterContext.parameter.type)
            }
        }
    }
}
