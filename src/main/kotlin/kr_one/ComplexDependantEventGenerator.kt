package kr_one

import org.jetbrains.letsPlot.GGBunch
import org.jetbrains.letsPlot.geom.geomPoint
import org.jetbrains.letsPlot.intern.Plot
import org.jetbrains.letsPlot.label.ggtitle
import org.jetbrains.letsPlot.letsPlot

fun main() {
    //remove annoying warning "Graphics2D from BufferedImage lacks BUFFERED_IMAGE hint", was actual for 1/2 PC
    System.setProperty("org.apache.batik.warn_destination", "false")

    drawExample()
    //invalidDataExample()
}

class ComplexDependantEventGenerator(
    val isRealRandom: Boolean,
    val isDebug: Boolean
) {
    private val randomEventGenerator = RandomEventGenerator(isRealRandom = isRealRandom, isDebug = false)

    operator fun invoke(Pa: Double, Pb: Double, PBdependantA: Double): Result {
        val resultA = randomEventGenerator(Pa)
        val resultB = randomEventGenerator(Pb)

        val PBdependant_notA = try {
            calculate_PBdepentant_notA(Pa, Pb, PBdependantA)
        } catch (e: Exception) {
            val exceptionResult =
                Result.InvalidData(
                    Pa,
                    Pb,
                    PBdependantA,
                    INVALID_PBDEPENDANT_NOTA,
                    resultA.randomNumber,
                    resultB.randomNumber
                )
            if (isDebug) println(exceptionResult.debugString())
            return exceptionResult
        }

        val complexDependantEventResult =
            if (resultA.result) { //x1<=Pa
                if (resultB.randomNumber <= PBdependantA) { //x2<=P(B/A)
                    Result.AB(Pa, Pb, PBdependantA, PBdependant_notA, resultA.randomNumber, resultB.randomNumber)
                } else { //x2>P(B/A)
                    Result.A_notB(Pa, Pb, PBdependantA, PBdependant_notA, resultA.randomNumber, resultB.randomNumber)
                }
            } else {  //x1>Pa
                if (resultB.randomNumber <= PBdependant_notA) { //x2<=P(B/!A)
                    Result.notA_B(Pa, Pb, PBdependantA, PBdependant_notA, resultA.randomNumber, resultB.randomNumber)
                } else { //x2>P(B/!A)
                    Result.notA_notB(Pa, Pb, PBdependantA, PBdependant_notA, resultA.randomNumber, resultB.randomNumber)
                }
            }
        if (isDebug) println(complexDependantEventResult.debugString())
        return complexDependantEventResult

    }

    /**
     * Возьмем за А - событие из RandomEventGenerator.
     * формула полной ветоятности:
     * P(B)=P(B/A)*P(A) +P(B/!A)*P(!A), отсюда выведем P(B/!A):
     *
     * P(B)-P(B/A)*P(A)
     * -------------- = P(B/!A)
     *      P(!A)
     *
     */
    @Throws(Exception::class)
    private fun calculate_PBdepentant_notA(Pa: Double, Pb: Double, PBdependantA: Double): Double {
        //если невозможно событие !A, то и P(B/!A) невозможно (+ деление на ноль)
        if (Pa == 1.0) {
            throw Exception("P(A) = 1.0")
        }
        //если невозможно событие B, то и зависимые события невозможны и задача не имеет смысла
        if (Pb == 0.0) {
            throw Exception("P(B) = 0.0")
        }
        //вероятность одной части уравнения не может быть больше P(B)
        if (PBdependantA * Pa > Pb) {
            throw Exception("P(B/A)*P(A) > P(B)")
        }

        val PBdependant_notA = (Pb - PBdependantA * Pa) / (1.0 - Pa)

        if (PBdependant_notA > 1.0 || PBdependant_notA < 0) {
            throw Exception("P(B/A) не валидно, данные не совместны")
        }

        return PBdependant_notA
    }

    sealed class Result(
        val Pa: Double,
        val Pb: Double,
        val PBdependant_A: Double,
        val PBdependant_notA: Double,
        val x1: Double,
        val x2: Double
    ) {
        /**
         * Деталь вышла с завода 1 и она хорошая
         */
        class AB(Pa: Double, Pb: Double, PBdependantA: Double, PBdependant_notA: Double, x1: Double, x2: Double) :
            Result(Pa, Pb, PBdependantA, PBdependant_notA, x1, x2)

        /**
         * Деталь вышла с завода 1 и она плохая
         */
        class A_notB(Pa: Double, Pb: Double, PBdependantA: Double, PBdependant_notA: Double, x1: Double, x2: Double) :
            Result(Pa, Pb, PBdependantA, PBdependant_notA, x1, x2)

        /**
         * Деталь вышла с завода 2 и она хорошая
         */
        class notA_B(Pa: Double, Pb: Double, PBdependantA: Double, PBdependant_notA: Double, x1: Double, x2: Double) :
            Result(Pa, Pb, PBdependantA, PBdependant_notA, x1, x2)

        /**
         * Деталь вышла с завода 2 и она плохая
         */
        class notA_notB(
            Pa: Double,
            Pb: Double,
            PBdependantA: Double,
            PBdependant_notA: Double,
            x1: Double,
            x2: Double
        ) :
            Result(Pa, Pb, PBdependantA, PBdependant_notA, x1, x2)

        class InvalidData(
            Pa: Double,
            Pb: Double,
            PBdependantA: Double,
            PBdependant_notA: Double = 0.0,
            x1: Double,
            x2: Double
        ) : Result(Pa, Pb, PBdependantA, PBdependant_notA, x1, x2)

        fun debugString(): String {
            return ("Pa = $Pa, Pb = $Pb, PBdependant_A = $PBdependant_A, PBdependant_notA = $PBdependant_notA"
                    //+ "\n ${this.javaClass.simpleName} : x1 = $x1, x2 = $x2"
                    )
        }

        fun graphTitle(): String = this.javaClass.simpleName
    }

    companion object {
        const val INVALID_PBDEPENDANT_NOTA = 0.5
    }
}

private fun drawExample() {
    val complexDependantEventGenerator = ComplexDependantEventGenerator(isRealRandom = true, isDebug = false)
    //хороший пример для отчета
//    val Pa = 0.6
//    val Pb = 0.3
//    val PBdependant_A = 0.5

    val Pa = 0.5
    val Pb = 0.3
    val PBdependant_A = 0.5
    //тогда P(B) = P(A)*P(B/A) + P(!A)*P(B/!A)
    //0.3 = 0.5*0.5 + (1-0.5)*x
    //искомое = 0.1, тогда !A!B должно быть 0.9 от всех !A, или для десяти тысяч тысяч
    //4500 и 500 для искомого
    val resultList = List<ComplexDependantEventGenerator.Result>(10000) {
        complexDependantEventGenerator.invoke(Pa, Pb, PBdependant_A)
    }
    println("Testing resultList with Pa = $Pa, Pb = $Pb, P(B/A) = $PBdependant_A, calc P(B/!A) = ${resultList.first().PBdependant_notA}")

    val ABList = resultList.filterIsInstance<ComplexDependantEventGenerator.Result.AB>().apply {
        println("AB size: $size")
    }
    val notA_BList = resultList.filterIsInstance<ComplexDependantEventGenerator.Result.notA_B>().apply {
        println("notA_B size: $size")
    }
    val A_notBList = resultList.filterIsInstance<ComplexDependantEventGenerator.Result.A_notB>().apply {
        println("A_notB size: $size")
    }
    val notA_notBList = resultList.filterIsInstance<ComplexDependantEventGenerator.Result.notA_notB>().apply {
        println("notA_notB size: $size")
    }
    val invalidDataList = resultList.filterIsInstance<ComplexDependantEventGenerator.Result.InvalidData>().apply {
        println("Invalid size: $size")
    }

    GGBunch()
        .addPlot(
            plotList(ABList), 0, 350, 300, 300
        )
        .addPlot(
            plotList(notA_BList), 350, 350, 300, 300
        )
        .addPlot(
            plotList(A_notBList), 0, 0, 300, 300
        )
        .addPlot(
            plotList(notA_notBList), 350, 0, 300, 300
        )
        .addPlot(
            plotList(invalidDataList), 700, 0, 300, 300
        )
        .show()
}

private fun plotList(list: List<ComplexDependantEventGenerator.Result>): Plot {
    val data = mapOf<String, List<*>>(
        "x1" to list.map { it.x1 },
        "x2" to list.map { it.x2 }
    )

    val p = letsPlot(data) { x = "x1"; y = "x2" } + ggtitle(list.firstOrNull()?.graphTitle() + " : ${list.size}")
    return (p + geomPoint(shape = 4))
}

private fun invalidDataExample() {
    val complexDependantEventGenerator = ComplexDependantEventGenerator(isRealRandom = true, isDebug = false)
    var Pa = 0.5
    var Pb = 0.25
    var PBdependant_A = 0.5

    val resultList = List<ComplexDependantEventGenerator.Result>(100000) {
        complexDependantEventGenerator.invoke(
            Pa,
            Pb,
            PBdependant_A
        )
    }
    printResult(resultList)

    println("Итерация по всем возможным Pa Pb P(B/A) с действительным рандомом")
    val resultList2 = mutableListOf<ComplexDependantEventGenerator.Result>()
    repeat(10) { times1 ->
        Pa = 0.1 * times1
        Pb = 0.0
        repeat(10) { times2 ->
            Pb = 0.1 * times2
            PBdependant_A = 0.0
            repeat(10) { times3 ->
                PBdependant_A = 0.1 * times3
                resultList2 += complexDependantEventGenerator.invoke(Pa, Pb, PBdependant_A)
            }
        }
    }
    printResult(resultList2)
}

private fun printResult(result: List<ComplexDependantEventGenerator.Result>) {
    result.filterIsInstance<ComplexDependantEventGenerator.Result.AB>().apply {
        println("AB size: $size")
    }
    result.filterIsInstance<ComplexDependantEventGenerator.Result.notA_B>().apply {
        println("notA_B size: $size")
    }
    result.filterIsInstance<ComplexDependantEventGenerator.Result.A_notB>().apply {
        println("A_notB size: $size")
    }
    result.filterIsInstance<ComplexDependantEventGenerator.Result.notA_notB>().apply {
        println("notA_notB size: $size")
    }
    result.filterIsInstance<ComplexDependantEventGenerator.Result.InvalidData>().apply {
        println("Invalid size: $size")
    }
}
