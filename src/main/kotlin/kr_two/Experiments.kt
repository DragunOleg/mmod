package kr_two

import kr_one.plotData
import org.apache.commons.math3.distribution.GammaDistribution

fun main() {
    builtGamma()
}

private fun builtGamma() {
    val n = 1000000
    val g = GammaDistribution(2.0, 2.0)
    val data = mapOf<String, Any>(
        "x" to List(n) { g.sample() }
    )
    plotData(data)
}
