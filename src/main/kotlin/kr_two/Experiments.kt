package kr_two

import kr_one.builtWeibull
import kr_one.plotData
import org.apache.commons.math3.distribution.GammaDistribution
import org.apache.commons.math3.distribution.LogNormalDistribution
import org.apache.commons.math3.distribution.WeibullDistribution
import org.apache.commons.math3.random.JDKRandomGenerator
import org.apache.commons.math3.random.RandomGenerator

fun main() {
    builtGamma()
}

private fun builtGamma() {
    val n = 1000000
    val rg: RandomGenerator = JDKRandomGenerator()
    val g = GammaDistribution(2.0, 2.0)
    val data = mapOf<String, Any>(
        "x" to List(n) { g.sample() }
    )
    plotData(data)
}
