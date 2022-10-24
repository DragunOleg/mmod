package kr_one

import org.jetbrains.letsPlot.GGBunch
import org.jetbrains.letsPlot.geom.geomHistogram
import org.jetbrains.letsPlot.geom.geomVLine
import org.jetbrains.letsPlot.ggsize
import org.jetbrains.letsPlot.label.ggtitle
import org.jetbrains.letsPlot.letsPlot
import kotlin.math.pow
import kotlin.math.sqrt

fun main() {
    //remove annoying warning "Graphics2D from BufferedImage lacks BUFFERED_IMAGE hint", was actual for 1/2 PC
    System.setProperty("org.apache.batik.warn_destination", "false")

    val randomEventGenerator = RandomEventGenerator(isRealRandom = true, isDebug = true)
    val Pa = 0.45
    randomEventGenerator.invoke(Pa)
    drawExample()
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

private fun drawExample() {
    val randomEventGenerator = RandomEventGenerator(isRealRandom = true, isDebug = false)
    val Pa = 0.5
    val sizes = listOf(10, 100, 1000, 10000, 100000)

    val bunch = GGBunch()
    sizes.forEachIndexed { index, n ->
        val data = mapOf<String, List<Double>>(
            "x" to List(n) { randomEventGenerator.invoke(Pa).randomNumber }
        )
        val mx = data["x"]!!.average()
        var dxSum = 0.0
        data["x"]!!.forEach {
            dxSum += (it - mx).pow(2)
        }
        //Дисперсия https://wiki.loginom.ru/articles/variance.html
        val dx = dxSum / n

        //Среднеквадратическое отклонение https://wiki.loginom.ru/articles/mean-square-deviation.html
        val sigma = sqrt(dx)
        val p = letsPlot(data) { x = "x" } + ggsize(320, 800) + ggtitle(
            "Средее для n=  $n:\n" +
                    "$mx\n" +
                    "D[X] = $dx\n" +
                    "σ = $sigma"
        )

        bunch.addPlot(
            p + geomHistogram(
                boundary = 0.0,
                binWidth = 0.1,
                color = "black",
                fill = "white"
            ) + geomVLine(
                xintercept = (data["x"] as List<Double>).average(),
                color = "red",
                linetype = "dashed"
            ), index * 300, 0, 290, 600
        )
    }
    bunch.show()

}
