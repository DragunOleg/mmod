package kr_one

import kotlin.test.Test
import kotlin.test.assertEquals

internal class RandomEventGeneratorTest {

    private val randomEventGenerator = RandomEventGenerator(isRealRandom = true, isDebug = false)

    @Test
    fun testBounds() {
        val resultList = List<RandomEventGenerator.Result>(100000) { randomEventGenerator.invoke(0.5) }
        assertEquals(false, resultList.any { it.randomNumber == 0.0 })
        assertEquals(false, resultList.any { it.randomNumber == 1.0 })
        val bounds1 = resultList.filter { it.randomNumber < 0.1 }.apply { println("bounds1 size = ${this.size}") }
        val bounds2 = resultList.filter { it.randomNumber >= 0.1 && it.randomNumber < 0.2 }.apply { println("bounds2 size = ${this.size}") }
        val bounds3 = resultList.filter { it.randomNumber >= 0.2 && it.randomNumber < 0.3 }.apply { println("bounds3 size = ${this.size}") }
        val bounds4 = resultList.filter { it.randomNumber >= 0.3 && it.randomNumber < 0.4 }.apply { println("bounds4 size = ${this.size}") }
        val bounds5 = resultList.filter { it.randomNumber >= 0.4 && it.randomNumber < 0.5 }.apply { println("bounds5 size = ${this.size}") }
        val bounds6 = resultList.filter { it.randomNumber >= 0.5 && it.randomNumber < 0.6 }.apply { println("bounds6 size = ${this.size}") }
        val bounds7 = resultList.filter { it.randomNumber >= 0.6 && it.randomNumber < 0.7 }.apply { println("bounds7 size = ${this.size}") }
        val bounds8 = resultList.filter { it.randomNumber >= 0.7 && it.randomNumber < 0.8 }.apply { println("bounds8 size = ${this.size}") }
        val bounds9 = resultList.filter { it.randomNumber >= 0.8 && it.randomNumber < 0.9 }.apply { println("bounds9 size = ${this.size}") }
        val bounds10 = resultList.filter { it.randomNumber >= 0.9 && it.randomNumber < 1.0 }.apply { println("bounds10 size = ${this.size}") }

    }

    @Test
    fun test1_0probability() {
        val resultList = List<RandomEventGenerator.Result>(100000) { randomEventGenerator.invoke(1.0) }
        assertEquals(true, resultList.all { it.result })
        assertEquals(true, resultList.all { it.Pa == 1.0 })
    }

    @Test
    fun test0_0probability() {
        val resultList = List<RandomEventGenerator.Result>(100000) { randomEventGenerator.invoke(0.0) }
        assertEquals(true, resultList.all { !it.result })
        assertEquals(true, resultList.all { it.Pa == 0.0 })
    }

    @Test
    fun testPseudoRandom() {
        val pseudoRandom1 = RandomEventGenerator(isRealRandom = false, isDebug = false)
        val pseudoRandom2 = RandomEventGenerator(isRealRandom = false, isDebug = false)

        val result1List = List<RandomEventGenerator.Result>(100000) { pseudoRandom1.invoke(0.5) }
        val result2List = List<RandomEventGenerator.Result>(100000) { pseudoRandom2.invoke(0.5) }

        assertEquals(result1List, result2List)
        assertEquals(true, result1List.subList(0, 1000) == result2List.subList(0, 1000))
        assertEquals(true, result1List[10000] == result2List[10000])
    }
}