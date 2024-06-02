package de.interact.domain.serialization

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.module.SimpleSerializers
import de.interact.domain.shared.InteractionTestId
import de.interact.domain.shared.UnitTestId
import java.util.*


class UnitTestIdSerializer : JsonSerializer<UnitTestId>() {
    override fun serialize(testId: UnitTestId, gen: JsonGenerator, sp: SerializerProvider) {
        gen.writeFieldName(testId.toString())
    }
}

class UnitTestIdDeserialize : KeyDeserializer() {
    override fun deserializeKey(key: String, ctx: DeserializationContext): UnitTestId {
        return UnitTestId(UUID.fromString(key))
    }
}

class InteractionTestIdSerializer : JsonSerializer<InteractionTestId>() {
    override fun serialize(testId: InteractionTestId, gen: JsonGenerator, sp: SerializerProvider) {
        gen.writeFieldName(testId.toString())
    }
}

class InteractionTestIdDeserialize : KeyDeserializer() {
    override fun deserializeKey(key: String, ctx: DeserializationContext): InteractionTestId {
        return InteractionTestId(UUID.fromString(key))
    }
}

class ValueClassSerializer(private val rawClass: Class<*>) : JsonSerializer<Any>() {
    override fun serialize(value: Any, gen: JsonGenerator, serializers: SerializerProvider) {
        if (rawClass.interfaces.isNotEmpty()) {
            gen.writeStartArray()
            gen.writeString(rawClass.simpleName)
        }
        gen.writeString(value.toString())
        if (rawClass.interfaces.isNotEmpty()) {
            gen.writeEndArray()
        }
    }

    override fun serializeWithType(
        value: Any,
        gen: JsonGenerator,
        serializers: SerializerProvider,
        typeSer: TypeSerializer
    ) {
        gen.writeStartArray()
        gen.writeString(rawClass.simpleName)
        gen.writeString(value.toString())
        gen.writeEndArray()
    }
}

object InteractModule : SimpleModule() {
    private fun readResolve(): Any = InteractModule

    init {
        _serializers = KotlinValueClassSerializers()
        this.addKeySerializer(
            UnitTestId::class.java,
            UnitTestIdSerializer()
        )
        this.addKeyDeserializer(
            UnitTestId::class.java,
            UnitTestIdDeserialize()
        )
        this.addKeySerializer(
            InteractionTestId::class.java,
            InteractionTestIdSerializer()
        )
        this.addKeyDeserializer(
            InteractionTestId::class.java,
            InteractionTestIdDeserialize()
        )

    }
}

internal class KotlinValueClassSerializers : SimpleSerializers() {

    private val cache = mutableMapOf<Class<*>, JsonSerializer<*>>()

    override fun findSerializer(
        config: SerializationConfig,
        type: JavaType,
        beanDesc: BeanDescription
    ): JsonSerializer<*>? {
        val rawClass = type.rawClass
        return if (rawClass.isAnnotationPresent(JvmInline::class.java)) {
            cache.getOrPut(rawClass) { ValueClassSerializer(rawClass) }
        } else {
            null
        }
    }
}