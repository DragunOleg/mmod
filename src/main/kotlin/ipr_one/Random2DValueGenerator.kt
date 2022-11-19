package ipr_one

import kr_one.RandomEventGenerator

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
    println("size=" +result.size)

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

            probMatrix.forEachIndexed outer@ { row, doubles ->
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
}