package de.interact.domain.testobservation.model

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.junit.jupiter.api.Test

internal class TestObservationTest {
    @Test
    fun `addObservedComponent should add a new component if it does not exist yet`() {
        val sut = TestObservation()
        val componentName = ComponentName("component")
        val componentVersion = ComponentVersion("version")

        val result = sut.addObservedComponent(componentName, componentVersion)

        result.name shouldBe componentName
        result.version shouldBe componentVersion
        sut.observedComponents shouldHaveSize 1
        sut.observedComponents shouldContain result
    }

    @Test
    fun `addObservedComponent should return an existing component if it already exists`() {
        val sut = TestObservation()
        val componentName = ComponentName("component")
        val componentVersion = ComponentVersion("version")
        val component = sut.addObservedComponent(componentName, componentVersion)

        val result = sut.addObservedComponent(componentName, componentVersion)

        result shouldBeSameInstanceAs component
        result.name shouldBe componentName
        result.version shouldBe componentVersion
        sut.observedComponents shouldHaveSize 1
        sut.observedComponents shouldContain result
    }
}