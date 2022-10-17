package kr_one

import kotlin.test.Test
import kotlin.test.assertEquals

internal class ComplexEventGeneratorTest {

    private val complexEventGenerator = ComplexEventGenerator(isRealRandom = true, isDebug = false)

    @Test
    fun testAtleastOne() {
        val resultList = List<ComplexEventGenerator.Result>(100000) {complexEventGenerator.invoke(0.5, 0.5)}
        assertEquals(true, resultList.any { it is ComplexEventGenerator.Result.AB })
        assertEquals(true, resultList.any { it is ComplexEventGenerator.Result.notA_B })
        assertEquals(true, resultList.any { it is ComplexEventGenerator.Result.A_notB })
        assertEquals(true, resultList.any { it is ComplexEventGenerator.Result.notA_notB })
    }

    @Test
    fun test0_5Distribution() {
        val resultList = List<ComplexEventGenerator.Result>(100000) {complexEventGenerator.invoke(0.5, 0.5)}
        println("Testing resultList with Pa = 0.5, Pb = 0.5")
        val ABList = resultList.filterIsInstance<ComplexEventGenerator.Result.AB>().apply {
            println("AB size = $size")
        }
        val notA_BList = resultList.filterIsInstance<ComplexEventGenerator.Result.notA_B>().apply {
            println("notA_B size = $size")
        }
        val A_notBList = resultList.filterIsInstance<ComplexEventGenerator.Result.A_notB>().apply {
            println("A_notB size = $size")
        }
        val notA_notBLIst = resultList.filterIsInstance<ComplexEventGenerator.Result.notA_notB>().apply {
            println("notA_notB size = $size")
        }
        assertEquals(true, ABList.size in 24000..26000)
        assertEquals(true, notA_BList.size in 24000..26000)
        assertEquals(true, A_notBList.size in 24000..26000)
        assertEquals(true, notA_notBLIst.size in 24000..26000)
        assertEquals(true, resultList.size == ABList.size + notA_BList.size + A_notBList.size + notA_notBLIst.size)

    }

    @Test
    fun testPa0Distribution() {
        val resultList = List<ComplexEventGenerator.Result>(100000) {complexEventGenerator.invoke(0.0, 0.5)}
        println("Testing resultList with Pa = 0.0, Pb = 0.5")
        val ABList = resultList.filterIsInstance<ComplexEventGenerator.Result.AB>().apply {
            println("AB size = $size")
        }
        val notA_BList = resultList.filterIsInstance<ComplexEventGenerator.Result.notA_B>().apply {
            println("notA_B size = $size")
        }
        val A_notBList = resultList.filterIsInstance<ComplexEventGenerator.Result.A_notB>().apply {
            println("A_notB size = $size")
        }
        val notA_notBLIst = resultList.filterIsInstance<ComplexEventGenerator.Result.notA_notB>().apply {
            println("notA_notB size = $size")
        }
        assertEquals(0, ABList.size)
        assertEquals(0, A_notBList.size)
        assertEquals(true, notA_BList.size in 49000..51000)
        assertEquals(true, notA_notBLIst.size in 49000..51000)
        assertEquals(true, resultList.size == ABList.size + notA_BList.size + A_notBList.size + notA_notBLIst.size)
    }

    @Test
    fun testPb0Distribution() {
        val resultList = List<ComplexEventGenerator.Result>(100000) {complexEventGenerator.invoke(0.5, 0.0)}
        println("Testing resultList with Pa = 0.5, Pb = 0.0")
        val ABList = resultList.filterIsInstance<ComplexEventGenerator.Result.AB>().apply {
            println("AB size = $size")
        }
        val notA_BList = resultList.filterIsInstance<ComplexEventGenerator.Result.notA_B>().apply {
            println("notA_B size = $size")
        }
        val A_notBList = resultList.filterIsInstance<ComplexEventGenerator.Result.A_notB>().apply {
            println("A_notB size = $size")
        }
        val notA_notBLIst = resultList.filterIsInstance<ComplexEventGenerator.Result.notA_notB>().apply {
            println("notA_notB size = $size")
        }
        assertEquals(0, ABList.size)
        assertEquals(0, notA_BList.size)
        assertEquals(true, A_notBList.size in 49000..51000)
        assertEquals(true,  notA_notBLIst.size in 49000..51000)
        assertEquals(true, resultList.size == ABList.size + notA_BList.size + A_notBList.size + notA_notBLIst.size)
    }
}
