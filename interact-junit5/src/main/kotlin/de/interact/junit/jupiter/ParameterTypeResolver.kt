package de.interact.junit.jupiter

import com.fasterxml.jackson.databind.JavaType
import de.interact.domain.serialization.SerializationConstants
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.params.converter.DefaultArgumentConverter
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

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
            //TODO use mapper
            SerializationConstants.mapper.readValue(argument.toString(), resolveTypeReference(parameterContext.parameter.parameterizedType))
        }
    }

    private fun resolveTypeReference(type: Type): JavaType {
        return if (type is ParameterizedType) {
            val genericTypes =
                type.actualTypeArguments.map { resolveTypeReference(it) }.toTypedArray()
            SerializationConstants.mapper.typeFactory.constructParametricType(
                type.rawType as Class<*>,
                *genericTypes
            )
        } else {
            SerializationConstants.mapper.typeFactory.constructType(type)
        }
    }
}
