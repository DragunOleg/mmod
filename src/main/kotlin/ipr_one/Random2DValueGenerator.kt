package ipr_one

import kr_one.RandomEventGenerator
import org.jetbrains.letsPlot.geom.geomHistogram
import org.jetbrains.letsPlot.ggsize
import org.jetbrains.letsPlot.letsPlot

fun main() {
    //remove annoying warning "Graphics2D from BufferedImage lacks BUFFERED_IMAGE hint", was actual for 1/2 PC
    System.setProperty("org.apache.batik.warn_destination", "false")
    val random2DValueGenerator = Random2DValueGenerator(isRealRandom = true, isDebug = true)
    val result = random2DValueGenerator.invoke(
        probMatrix = arrayOf(
            doubleArrayOf(1 / 8.0, 2 / 8.0, 2 / 8.0),
            doubleArrayOf(0.0, 0.0, 3 / 8.0)
        ),
        vectorA = listOf(10, 20, 30),
        vectorB = listOf(40, 50),
        RVN = 10
    )
    println("size=" + result.size)

}

class Random2DValueGenerator(
    val isRealRandom: Boolean,
    val isDebug: Boolean
) {
    private val randomEventGenerator = RandomEventGenerator(isRealRandom = isRealRandom, isDebug = false)

    operator fun invoke(
        probMatrix: Array<DoubleArray>,
        vectorA: List<Int>,
        vectorB: List<Int>,
        RVN: Int
    ): List<Result> {
        val result: MutableList<Result> = mutableListOf()
        if (isDebug) {
            probMatrix.forEachIndexed { index, doubles ->
                println("строка $index значения:")
                doubles.forEach {
                    print("$it;")
                }
                print("\n")
            }
        }
        repeat(RVN) {
            var randomNumber = randomEventGenerator.invoke(0.5).randomNumber
            var tempSumToCompare = 0.0

            probMatrix.forEachIndexed { row, doubles ->
                doubles.forEachIndexed { column, d ->
                    tempSumToCompare += d
                    if (randomNumber <= tempSumToCompare) {
                        result += Result(
                            a = vectorA[column],
                            b = vectorB[row],
                            row = row,
                            column = column,
                            randomNumber = randomNumber
                        )
                        if (isDebug) {
                            println("число $randomNumber")
                            println("строка $row")
                            println("столбец $column")
                        }
                        //to not add more numbers
                        randomNumber = 2.0
                    }
                }
            }
        }
        return result
    }

    data class Result(
        val a: Int,
        val b: Int,
        val row: Int,
        val column: Int,
        val randomNumber: Double
    )

    fun drawVectorsHist(list: List<Result>, probMatrix: Array<DoubleArray>,vectorA: List<Int>, vectorB: List<Int>,) {
        val possibleEventSublist: MutableList<Triple<Int, Int, String>> = mutableListOf()
        probMatrix.forEachIndexed { row, doubles ->
            doubles.forEachIndexed { column, d ->
                possibleEventSublist += Triple(row, column, "b$row=${vectorB[row]};a$column=${vectorA[column]}")
            }
        }
        val resultCondList: MutableList<String> = mutableListOf()
        val resultXList: MutableList<Double> = mutableListOf()

        possibleEventSublist.forEach { possibleEvent ->
            val resultXListIteration = list
                .filter { it.row == possibleEvent.first && it.column == possibleEvent.second }
                .map { it.randomNumber }
            resultCondList += list
                .filter { it.row == possibleEvent.first && it.column == possibleEvent.second }
                .map { possibleEvent.third }
            resultXList += resultXListIteration
            println("event ${possibleEvent.third}: size = ${resultXListIteration.size}")
        }

        val data = mapOf<String, Any>(
            "cond" to resultCondList,
            "x" to resultXList
        )
        val p = letsPlot(data) {x = "x"; fill = "cond"} + ggsize(800, 500)
        (p+ geomHistogram(binWidth = 0.05)).show()
    }
}