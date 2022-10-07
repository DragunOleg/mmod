package kr_one

import org.jetbrains.letsPlot.GGBunch
import org.jetbrains.letsPlot.geom.geomPoint
import org.jetbrains.letsPlot.ggsize
import org.jetbrains.letsPlot.intern.Plot
import org.jetbrains.letsPlot.label.ggtitle
import org.jetbrains.letsPlot.letsPlot

fun main() {
    val complexEventGenerator = ComplexEventGenerator(isRealRandom = true, isDebug = true)
    val Pa = 0.45
    val Pb = 0.9
    complexEventGenerator.invoke(Pa, Pb)

    drawTest()
}

class ComplexEventGenerator(
    val isRealRandom: Boolean,
    val isDebug: Boolean
) {
    private val randomEventGenerator = RandomEventGenerator(isRealRandom = isRealRandom, isDebug = false)

    operator fun invoke(Pa: Double, Pb: Double): Result {
        val resultA = randomEventGenerator.invoke(Pa)
        val resultB = randomEventGenerator.invoke(Pb)

        val complexEventResult =
            if (resultA.result) {
                if (resultB.result) {
                    Result.AB(Pa = Pa, x1 = resultA.randomNumber, Pb = Pb, x2 = resultB.randomNumber)
                } else {
                    Result.A_notB(Pa = Pa, x1 = resultA.randomNumber, Pb = Pb, x2 = resultB.randomNumber)
                }
            } else {
                if (resultB.result) {
                    Result.notA_B(Pa = Pa, x1 = resultA.randomNumber, Pb = Pb, x2 = resultB.randomNumber)
                } else {
                    Result.notA_notB(Pa = Pa, x1 = resultA.randomNumber, Pb = Pb, x2 = resultB.randomNumber)
                }
            }
        if (isDebug) println(complexEventResult.debugString())
        return complexEventResult
    }

    sealed class Result(
        val Pa: Double,
        val x1: Double,
        val Pb: Double,
        val x2: Double
    ) {
        class AB(Pa: Double, x1: Double, Pb: Double, x2: Double) : Result(Pa, x1, Pb, x2)
        class A_notB(Pa: Double, x1: Double, Pb: Double, x2: Double) : Result(Pa, x1, Pb, x2)
        class notA_B(Pa: Double, x1: Double, Pb: Double, x2: Double) : Result(Pa, x1, Pb, x2)
        class notA_notB(Pa: Double, x1: Double, Pb: Double, x2: Double) : Result(Pa, x1, Pb, x2)

        fun debugString(): String {
            return ("${this.javaClass.simpleName} :" +
                    "Pa = $Pa, x1 = $x1, Pb = $Pb, x2 = $x2")
        }

        fun graphTitle(): String = ("${this.javaClass.simpleName} :" +
                "Pa = $Pa, Pb = $Pb")
    }
}

private fun drawTest() {
    val complexEventGenerator = ComplexEventGenerator(isRealRandom = true, isDebug = false)
    val Pa = 0.5
    val Pb = 0.8
    val resultList = List<ComplexEventGenerator.Result>(10000) { complexEventGenerator.invoke(Pa, Pb) }
    println("Testing resultList with Pa = $Pa, Pb = $Pb")
    val ABList = resultList.filterIsInstance<ComplexEventGenerator.Result.AB>().apply {
        println("AB size = $size")
    }
    val notA_BList = resultList.filterIsInstance<ComplexEventGenerator.Result.notA_B>().apply {
        println("notA_B size = $size")
    }
    val A_notBList = resultList.filterIsInstance<ComplexEventGenerator.Result.A_notB>().apply {
        println("A_notB size = $size")
    }
    val notA_notBList = resultList.filterIsInstance<ComplexEventGenerator.Result.notA_notB>().apply {
        println("notA_notB size = $size")
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
        .show()
}

private fun plotList(list: List<ComplexEventGenerator.Result>): Plot {
    val data = mapOf<String, List<*>>(
        "x" to list.map { it.x1 },
        "y" to list.map { it.x2 }
    )

    val p = letsPlot(data) { x = "x"; y = "y" } + ggtitle(list.first().graphTitle()) //+ ggsize(300,300)
    return (p + geomPoint(shape = 1))
}