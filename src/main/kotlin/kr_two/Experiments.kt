package kr_two

import kr_one.RandomEventGenerator
import kr_one.plotData
import org.apache.commons.math3.distribution.GammaDistribution
import org.jetbrains.letsPlot.geom.geomHistogram
import org.jetbrains.letsPlot.geom.geomVLine
import org.jetbrains.letsPlot.ggsize
import org.jetbrains.letsPlot.letsPlot
import kotlin.math.ln

fun main() {
    //remove annoying warning "Graphics2D from BufferedImage lacks BUFFERED_IMAGE hint", was actual for 1/2 PC
    System.setProperty("org.apache.batik.warn_destination", "false")
    //builtGamma()
    tryErlang()
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
    val n = 10
    val mu = 2.0
    val data = mapOf<String, List<Double>>(
        "x" to List(10000) { smallErlang(randomEventGenerator, n, mu) }
    )
    plotData(data)
    // val testBounds = data["x"]!!.sorted()

//    val p = letsPlot(data) { x = "x" } + ggsize(700, 800)
//    (p + geomHistogram(binWidth = 1)).show()

}

//https://www.win.tue.nl/~marko/2WB05/lecture8.pdf
private fun smallErlang(generator: RandomEventGenerator, n: Int, mu: Double): Double {
    var multiplicationResult = 1.0
    val data = List(n) { generator.invoke(Pa = 1.0).randomNumber }.map { multiplicationResult *= it }
    return -ln(multiplicationResult) / mu
}
