package kr_two

import kr_one.RandomEventGenerator
import kr_one.plotData
import org.apache.commons.math3.distribution.GammaDistribution
import org.jetbrains.letsPlot.geom.*
import org.jetbrains.letsPlot.ggsize
import org.jetbrains.letsPlot.label.ggtitle
import org.jetbrains.letsPlot.letsPlot
import kotlin.math.E
import kotlin.math.ln
import kotlin.math.pow

fun main() {
    //remove annoying warning "Graphics2D from BufferedImage lacks BUFFERED_IMAGE hint", was actual for 1/2 PC
    System.setProperty("org.apache.batik.warn_destination", "false")
    //builtGamma()
    //tryErlang()
    erlangTheoretical()
    //tryRandomAndTheoretical()
}

private fun builtGamma() {
    val n = 1000000
    val g = GammaDistribution(2.0, 2.0)
    val data = mapOf<String, Any>(
        "x" to List(n) { g.sample() }
    )
    plotData(data)
}

private fun tryErlang() {
    val randomEventGenerator = RandomEventGenerator(isRealRandom = true, isDebug = false)
    val n = 0
    val mu = 0.0
    val data = mapOf<String, List<Double>>(
        "x" to List(10000) { smallErlang(randomEventGenerator, n, mu) }
    )
    //plotData(data)
    // val testBounds = data["x"]!!.sorted()

    val p = letsPlot(data) { x = "x" } + ggsize(700, 800)
    (p + geomHistogram(binWidth = 1)).show()

}

//https://www.win.tue.nl/~marko/2WB05/lecture8.pdf
private fun smallErlang(generator: RandomEventGenerator, n: Int, mu: Double): Double {
    var multiplicationResult = 1.0
    val data = List(n) { generator.invoke(Pa = 1.0).randomNumber }.map { multiplicationResult *= it }
    return -ln(multiplicationResult) / mu
}

private fun erlangTheoretical() {
    val n = 1
    val mu = 0.5
    val xMultiplier = 0.1
    val listX = List(150) { it * xMultiplier }
    val listY = listX.map { calculateErlangY(n, mu, it) }

    val data = mapOf<String, List<*>>(
        "x" to listX,
        "y" to listY
    )

    val p = letsPlot(data) { x = "x"; y = "y" } + ggsize(500, 500)
    //(p+geomPoint(shape = 5)).show()
    (p + geomLine { y = "y" } + ggtitle("Теоретическое распределение Эрланга","при n = $n; μ = $mu; с шагом $xMultiplier")).show()
}

private fun calculateErlangY(n: Int, mu: Double, x: Double): Double {
    val result = (mu.pow(n) * x.pow(n - 1) * E.pow(-mu * x)) / factorial(n-1)
    return result
}

fun factorial(num: Int) =  if (num == 0) {
    1
} else {
    (1..num).reduce { a, b -> a * b }
}

private fun tryRandomAndTheoretical() {
    val randomEventGenerator = RandomEventGenerator(isRealRandom = true, isDebug = false)
    val n = 10
    val mu = 2.0
    val theoreticalXFrequency = 0.01
    val listTheoreticalX = List(1500) { it * theoreticalXFrequency }
    val listTheoreticalY = listTheoreticalX.map { calculateErlangY(n, mu, it) }

    val randomValuesCount = 10000

    val data = mapOf<String, List<*>>(
        "x" to listTheoreticalX,
        "y" to listTheoreticalY,
        "xReal" to List(randomValuesCount) { smallErlang(randomEventGenerator, n, mu) }
    )

    val p = letsPlot(data) { x = "x"; y = "y"; z = "xReal" } + ggsize(500, 500)

    geomJitter()
    (p + geomLine { y = "y"; }).show()

}
