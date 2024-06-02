package de.interact.junit.jupiter

import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ParameterContext
import java.lang.reflect.Executable
import java.lang.reflect.Parameter
import java.lang.reflect.ParameterizedType

@ExtendWith(MockKExtension::class)
internal class ParameterTypeResolverTest {

    @MockK
    private lateinit var parameterContext: ParameterContext

    @MockK
    private lateinit var parameter: Parameter


    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    @Test
    fun `should resolve null parameter`() {
        val result = ParameterTypeResolver.resolveArgumentToParameterType(parameterContext, null)

        result shouldBe null
    }

    @Test
    fun `should resolve string parameter`() {
        every { parameterContext.parameter } returns parameter
        every { parameter.type } returns String::class.java

        val result = ParameterTypeResolver.resolveArgumentToParameterType(parameterContext, "string")

        result shouldBe "string"
    }

    @Test
    fun `should resolve primitive parameter`() {
        val executable: Executable = mockk {
            every { declaringClass } returns ParameterTypeResolverTest::class.java
        }
        every { parameterContext.parameter } returns parameter
        every { parameterContext.declaringExecutable } returns executable
        every { parameter.type } returns Int::class.javaPrimitiveType

        val result = ParameterTypeResolver.resolveArgumentToParameterType(parameterContext, "1")

        result shouldBe 1
    }

    @Test
    fun `should resolve parameterized parameter`() {
        every { parameterContext.parameter } returns parameter
        every { parameter.type } returns List::class.java
        every { parameter.parameterizedType } returns List::class.java.genericSuperclass

        val result = ParameterTypeResolver.resolveArgumentToParameterType(parameterContext, "[1,2,3]")

        result shouldBe listOf(1, 2, 3)
    }

    @Test
    fun `should resolve complex parameterized parameter`() {
        every { parameterContext.parameter } returns parameter
        every { parameter.type } returns List::class.java
        every { parameter.parameterizedType } returns List::class.java.genericSuperclass

        val result = ParameterTypeResolver.resolveArgumentToParameterType(parameterContext, "[{\"name\":\"Test\"}]")

        result shouldBe listOf(mapOf("name" to "Test"))
    }

    @Test
    fun `should resolve complex parameter`() {
        every { parameterContext.parameter } returns parameter
        every { parameter.type } returns Point::class.java
        every { parameter.parameterizedType } returns Point::class.java

        val result = ParameterTypeResolver.resolveArgumentToParameterType(parameterContext, "{\"x\":1,\"y\":2}")

        result shouldBe Point(1, 2)
    }

    @Test
    fun `should resolve parameterized parameter of complex type`() {

        val parameterizedType: ParameterizedType = mockk {
            every { actualTypeArguments } returns arrayOf(Point::class.java)
        }
        every { parameterContext.parameter } returns parameter
        every { parameter.type } returns List::class.java
        every { parameter.parameterizedType } returns parameterizedType

        val result = ParameterTypeResolver.resolveArgumentToParameterType(parameterContext, "[{\"x\":1,\"y\":2}]")

        result shouldBe listOf(Point(1, 2))
    }
}

internal data class Point(val x: Int, val y: Int)