package kr_one

import kotlin.test.Test
import kotlin.test.assertEquals

internal class RandomEventGeneratorTest {

    private val randomEventGenerator = RandomEventGenerator(isRealRandom = true, isDebug = false)

    @Test
    fun testBounds() {
        val resultList = List<RandomEventGenerator.Result>(100000) {randomEventGenerator.invoke(0.5)}
        assertEquals(false, resultList.any{it.randomNumber == 0.0})
        assertEquals(false, resultList.any{it.randomNumber == 1.0})
    }

    @Test
    fun test1_0probability() {
        val resultList = List<RandomEventGenerator.Result>(100000) {randomEventGenerator.invoke(1.0)}
        assertEquals(true, resultList.all { it.result })
        assertEquals(true, resultList.all { it.Pa == 1.0 })
    }

    @Test
    fun test0_0probability() {
        val resultList = List<RandomEventGenerator.Result>(100000) {randomEventGenerator.invoke(0.0)}
        assertEquals(true, resultList.all { !it.result })
        assertEquals(true, resultList.all { it.Pa == 0.0 })
    }

    @Test
    fun testPseudoRandom() {
        val pseudoRandom1 = RandomEventGenerator(isRealRandom = false, isDebug = false)
        val pseudoRandom2 = RandomEventGenerator(isRealRandom = false, isDebug = false)

        val result1List = List<RandomEventGenerator.Result>(100000) {pseudoRandom1.invoke(0.5)}
        val result2List = List<RandomEventGenerator.Result>(100000) {pseudoRandom2.invoke(0.5)}

        assertEquals(result1List, result2List)
        assertEquals(true, result1List.subList(0, 10) == result2List.subList(0,10))
        assertEquals(true, result1List[10000] == result2List[10000])
    }
}