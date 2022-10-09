package kr_one

import org.jetbrains.letsPlot.geom.geomHistogram
import org.jetbrains.letsPlot.geom.geomVLine
import org.jetbrains.letsPlot.ggsize
import org.jetbrains.letsPlot.letsPlot

fun main() {
    val randomEventGenerator = RandomEventGenerator(isRealRandom = true, isDebug = true)
    val Pa = 0.45
    randomEventGenerator.invoke(Pa)
    drawTest()
}

/**
 * @param isRealRandom: true if you want real random. False if you want repeatable pseudoRandom
 * @param isDebug: should println results or not
 */
class RandomEventGenerator(
    private val isRealRandom: Boolean,
    private val isDebug: Boolean
) {
    private val realRandom = java.util.Random()
    private val pseudoRandom = kotlin.random.Random(10)

    private fun realRandomNext() = realRandom.nextDouble()
    private fun pseudoRandomNext() = pseudoRandom.nextDouble()

    /**
     * Returning true/false if event happened, gained probability and generated x
     * @see Result
     */
    operator fun invoke(Pa: Double): Result {
        val randomNumberX = if (isRealRandom) realRandomNext() else pseudoRandomNext()
        val result = randomNumberX <= Pa

        if (isDebug) {
            println(
                "x = $randomNumberX \n" +
                        "Pa = $Pa"
            )
            if (result) {
                println("Event happened")
            } else {
                println("Event didn't happened")
            }
        }

        return Result(result = result, Pa = Pa, randomNumber = randomNumberX)
    }

    /**
     * @param result is true, if event happened
     * @param Pa given probability
     * @param randomNumber number from >0 & <1
     */
    data class Result(
        val result: Boolean,
        val Pa: Double,
        val randomNumber: Double
    )
}

private fun drawTest() {
    val randomEventGenerator = RandomEventGenerator(isRealRandom = true, isDebug = false)
    val Pa = 0.5
    val data = mapOf<String, List<Double>>(
        "x" to List(1000000) { randomEventGenerator.invoke(Pa).randomNumber }
    )
    val p = letsPlot(data) { x = "x" } + ggsize(700, 500)

    //(p + geomHistogram(binWidth = 0.01)).show()

    (p + geomHistogram(
        binWidth = 0.05,
        color = "black",
        fill = "white"
    ) + geomVLine(
        xintercept = (data["x"] as List<Double>).average(),
        color = "red",
        linetype = "dashed",
        size = 3.0
    )).show()
}
