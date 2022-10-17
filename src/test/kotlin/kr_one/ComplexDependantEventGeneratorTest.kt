package kr_one

import kotlin.test.Test
import kotlin.test.assertEquals

internal class ComplexDependantEventGeneratorTest {

    private val complexDependantEventGenerator = ComplexDependantEventGenerator(isRealRandom = true, isDebug = false)

    @Test
    fun test0_5Distributions() {
        val Pa = 0.5
        val Pb = 0.5
        val PBdependantA = 0.5
        val resultList = List<ComplexDependantEventGenerator.Result>(100000) {
            complexDependantEventGenerator.invoke(
                Pa,
                Pb,
                PBdependantA
            )
        }
        assertEquals(true, resultList.filterIsInstance<ComplexDependantEventGenerator.Result.InvalidData>().isEmpty())
        println("Testing resultList with Pa = $Pa, Pb = $Pb, P(B/A) = $PBdependantA")
        val ABList = resultList.filterIsInstance<ComplexDependantEventGenerator.Result.AB>().apply {
            println("AB size = $size")
        }
        val notA_BList = resultList.filterIsInstance<ComplexDependantEventGenerator.Result.notA_B>().apply {
            println("notA_B size = $size")
        }
        val A_notBList = resultList.filterIsInstance<ComplexDependantEventGenerator.Result.A_notB>().apply {
            println("A_notB size = $size")
        }
        val notA_notBLIst = resultList.filterIsInstance<ComplexDependantEventGenerator.Result.notA_notB>().apply {
            println("notA_notB size = $size")
        }
        assertEquals(true, ABList.size in 24000..26000)
        assertEquals(true, notA_BList.size in 24000..26000)
        assertEquals(true, A_notBList.size in 24000..26000)
        assertEquals(true, notA_notBLIst.size in 24000..26000)
        assertEquals(true, resultList.size == ABList.size + notA_BList.size + A_notBList.size + notA_notBLIst.size)
    }

    @Test
    fun test0_1and0_9params() {
        val Pa = 0.5
        val Pb = 0.3
        val PBdependantA = 0.5
        //тогда P(B) = P(A)*P(B/A) + P(!A)*P(B/!A)
        //0.3 = 0.5*0.5 + (1-0.5)*x
        //искомое = 0.1, тогда !A!B должно быть 0.9 от всех !A, или для ста тысяч
        //45 тысяч и 5 тысяч
        val resultList = List<ComplexDependantEventGenerator.Result>(100000) {
            complexDependantEventGenerator.invoke(
                Pa,
                Pb,
                PBdependantA
            )
        }
        println("Testing resultList with Pa = $Pa, Pb = $Pb, P(B/A) = $PBdependantA")
        val ABList = resultList.filterIsInstance<ComplexDependantEventGenerator.Result.AB>().apply {
            println("AB size = $size")
        }
        val notA_BList = resultList.filterIsInstance<ComplexDependantEventGenerator.Result.notA_B>().apply {
            println("notA_B size = $size")
        }
        val A_notBList = resultList.filterIsInstance<ComplexDependantEventGenerator.Result.A_notB>().apply {
            println("A_notB size = $size")
        }
        val notA_notBLIst = resultList.filterIsInstance<ComplexDependantEventGenerator.Result.notA_notB>().apply {
            println("notA_notB size = $size")
        }
        assertEquals(true, ABList.size in 24000..26000)
        assertEquals(true, notA_BList.size in 4000..6000)
        assertEquals(true, A_notBList.size in 24000..26000)
        assertEquals(true, notA_notBLIst.size in 44000..46000)
        assertEquals(true, resultList.size == ABList.size + notA_BList.size + A_notBList.size + notA_notBLIst.size)

    }

    @Test
    fun testOpenParams() {
        val Pa = 0.7
        val Pb = 0.7
        val PBdependantA = 0.5
        val resultList = List<ComplexDependantEventGenerator.Result>(100000) {
            complexDependantEventGenerator.invoke(
                Pa,
                Pb,
                PBdependantA
            )
        }
        println("Testing resultList with Pa = $Pa, Pb = $Pb, P(B/A) = $PBdependantA")
        val ABList = resultList.filterIsInstance<ComplexDependantEventGenerator.Result.AB>().apply {
            println("AB size = $size")
        }
        val notA_BList = resultList.filterIsInstance<ComplexDependantEventGenerator.Result.notA_B>().apply {
            println("notA_B size = $size")
        }
        val A_notBList = resultList.filterIsInstance<ComplexDependantEventGenerator.Result.A_notB>().apply {
            println("A_notB size = $size")
        }
        val notA_notBLIst = resultList.filterIsInstance<ComplexDependantEventGenerator.Result.notA_notB>().apply {
            println("notA_notB size = $size")
        }
        val invalidData = resultList.filterIsInstance<ComplexDependantEventGenerator.Result.InvalidData>().apply {
            println("invalid size = $size")
        }
        assertEquals(true, resultList.filterIsInstance<ComplexDependantEventGenerator.Result.InvalidData>().isEmpty())

    }
}
