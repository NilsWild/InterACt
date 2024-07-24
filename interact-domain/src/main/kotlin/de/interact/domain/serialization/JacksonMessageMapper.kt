package de.interact.domain.serialization

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class JacksonMessageMapper(private val mapper: ObjectMapper, override val order: Int = Integer.MIN_VALUE): MessageMapper {
    override fun canHandle(type: Class<*>): Boolean {
        return true
    }

    override fun resolveToTestParameter(value: String, type: Type): Any? {
        return mapper.readValue(value,resolveTypeReference(type))

    }

    private fun resolveTypeReference(type: Type): JavaType {
        return if (type is ParameterizedType) {
            val genericTypes =
                type.actualTypeArguments.map { resolveTypeReference(it) }.toTypedArray()
            mapper.typeFactory.constructParametricType(
                type.rawType as Class<*>,
                *genericTypes
            )
        } else {
            mapper.typeFactory.constructType(type)
        }
    }


    override fun writeValueAsJsonString(value: Any): String {
        return mapper.writeValueAsString(value)
    }

}