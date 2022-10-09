package kr_one

import kotlin.test.Test
import kotlin.test.assertEquals

internal class WholeGroupEventGeneratorTest {

    private val wholeGroupEventGenerator = WholeGroupEventGenerator(isReadRandom = true, isDebug = false)

    private val groupToTest = listOf<Double>(
        0.0,
        (2.0 / 16),
        (4.0 / 16),
        (10.0 / 16)
    )


    @Test
    fun testBounds() {
        val resultList = List<WholeGroupEventGenerator.Result>(100000) {
            wholeGroupEventGenerator.invoke(groupToTest)
        }
        assertEquals(false, resultList.any { it.indexBelonging == 0 })
        assertEquals(false, resultList.any { it.indexBelonging > groupToTest.lastIndex })
        assertEquals(false, resultList.any { it.randomNumber <= 0.0 })
        assertEquals(false, resultList.any { it.randomNumber >= 1.0 })
    }

}