package de.interact.utils

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class MultiMapUnitTest {

    @Test
    fun `when put one element then it should have size 1`() {
        val multiMap = MultiMap<String, Int>()
        multiMap.put("even", 2)

        multiMap["even"] shouldHaveSize 1
        multiMap.size shouldBe 1
    }

    @Test
    fun `if no elements were put then it should have size 0`() {
        val multiMap = MultiMap<String, Int>()

        multiMap.size shouldBe 0
    }

    @Test
    fun `when put two elements with different keys then it should return two keys`() {
        val multiMap = MultiMap<String, Int>()
        multiMap.put("even", 2)
        multiMap.put("odd", 1)

        multiMap.keys shouldBe hashSetOf("even", "odd")
    }

    @Test
    fun `when put two elements with different keys then it should return two entries`() {
        val multiMap = MultiMap<String, Int>()
        multiMap.put("even", 2)
        multiMap.put("odd", 1)

        multiMap.entries shouldHaveSize 2
    }

    @Test
    fun `when put two elements with different keys then it should return two values`() {
        val multiMap = MultiMap<String, Int>()
        multiMap.put("even", 2)
        multiMap.put("odd", 1)

        multiMap.values shouldBe hashSetOf(1, 2)
    }

    @Test
    fun `when put two elements with different keys then it should return values of given key`() {
        val multiMap = MultiMap<String, Int>()
        multiMap.put("even", 2)
        multiMap.put("odd", 1)

        multiMap["even"] shouldBe hashSetOf(2)
    }

    @Test
    fun `when put two elements with same key then it should have one key and two values`() {
        val multiMap = MultiMap<String, Int>()
        multiMap.put("even", 2)
        multiMap.put("even", 4)

        multiMap["even"] shouldHaveSize 2
        multiMap.keys shouldHaveSize 1
        multiMap.values shouldHaveSize 2
    }

    @Test
    fun `when put two elements at the same time then it should have one key and two values`() {
        val multiMap = MultiMap<String, Int>()
        multiMap.putAll("even", setOf(2, 4))

        multiMap["even"] shouldHaveSize 2
        multiMap.keys shouldHaveSize 1
        multiMap.values shouldHaveSize 2
    }

    @Test
    fun `when put one element and remove the same element then it should be empty for given key`() {
        val multiMap = MultiMap<String, Int>()
        multiMap.put("even", 2)
        multiMap.remove("even", 2)

        multiMap["even"] shouldHaveSize 0
    }

    @Test
    fun `when put three element and remove one then it should keep the other two for given key`() {
        val multiMap = MultiMap<String, Int>()
        multiMap.putAll("even", setOf(2, 4, 6))

        multiMap.remove("even", 2)

        multiMap["even"] shouldHaveSize 2
        multiMap["even"] shouldContainAll setOf(4, 6)
    }

    @Test
    fun `when put two elements and remove all then it should have zero elements for the given key`() {
        val multiMap = MultiMap<String, Int>()
        multiMap.putAll("even", setOf(2, 4))

        multiMap.removeAll("even")

        multiMap["even"] shouldHaveSize 0
    }

    @Test
    fun `when put two elements and clear all then it should have zero elements for the given key`() {
        val multiMap = MultiMap<String, Int>()
        multiMap.putAll("even", setOf(2, 4))

        multiMap.clear("even")

        multiMap.containsKey("even") shouldBe false
    }

    @Test
    fun `when put one element and replacing it then it should have the new element for given key`() {
        val multiMap = MultiMap<String, Int>()
        multiMap.put("even", 2)
        multiMap.replace("even", 2, 4)

        multiMap["even"] shouldContain 4
        multiMap["even"] shouldNotContain 2
    }

    @Test
    fun `when replacing a elements not contained for the key then it should not add and return false`() {
        val multiMap = MultiMap<String, Int>()
        multiMap.put("even", 2)
        val wasReplaced = multiMap.replace("even", 4, 6)

        wasReplaced shouldBe false
        multiMap["even"] shouldNotContain 4
        multiMap["even"] shouldContain 2
    }

    @Test
    fun `when put two elements and replacing it then it should have the new element for given key`() {
        val multiMap = MultiMap<String, Int>()
        multiMap.putAll("even", setOf(2, 4))
        multiMap.replaceAll("even", setOf(6))

        multiMap["even"] shouldContain 6
        multiMap["even"] shouldNotContain 2
        multiMap["even"] shouldNotContain 4
    }

    @Test
    fun `when put one element then it should contain the key`() {
        val multiMap = MultiMap<String, Int>()
        multiMap.put("even", 2)

        multiMap.containsKey("even") shouldBe true
    }

    @Test
    fun `when put one element then it should contain the value`() {
        val multiMap = MultiMap<String, Int>()
        multiMap.put("even", 2)

        multiMap.containsValue(2) shouldBe true
    }

    @Test
    fun `when no element was added then it should be empty`() {
        val multiMap = MultiMap<String, Int>()

        multiMap.isEmpty() shouldBe true
    }

    @Test
    fun `when put one element and call clear then it should be empty`() {
        val multiMap = MultiMap<String, Int>()
        multiMap.put("even", 2)
        multiMap.clear()

        multiMap["even"].isEmpty() shouldBe true
    }
}