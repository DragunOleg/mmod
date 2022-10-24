package kr_two

import kr_one.RandomEventGenerator
import org.jetbrains.letsPlot.GGBunch
import org.jetbrains.letsPlot.geom.geomDensity
import org.jetbrains.letsPlot.geom.geomHistogram
import org.jetbrains.letsPlot.geom.geomLine
import org.jetbrains.letsPlot.geom.geomVLine
import org.jetbrains.letsPlot.label.ggtitle
import org.jetbrains.letsPlot.letsPlot
import kotlin.math.E
import kotlin.math.ln
import kotlin.math.pow

//todo тесты класса
//todo посчитать кси-квадрат https://wiki.loginom.ru/articles/chi-square-test.html
fun main() {
    //remove annoying warning "Graphics2D from BufferedImage lacks BUFFERED_IMAGE hint", was actual for 1/2 PC
    System.setProperty("org.apache.batik.warn_destination", "false")

    val erlangDistributionGenerator = ErlangDistributionGenerator(isRealRandom = true, isDebug = true)
    val n = 10
    val mu = 2.0
    repeat(10) {
        erlangDistributionGenerator(n = n, mu = mu)
    }

    drawGraphs()
    //invalidDataExample()
}

/**
 * Получаем U1...Un случайных величин на интервале 0..1
 * Перемножаем их друг на друга
 * Случайная величина из распределения Эрланга равна
 * x = -ln(U1*U2*...*Un)/mu
 * @see <a href="https://www.win.tue.nl/~marko/2WB05/lecture8.pdf">математическое обоснование, страница 9</a>
 */
class ErlangDistributionGenerator(
    val isRealRandom: Boolean,
    val isDebug: Boolean
) {
    //отсюда будем получать x (0-1)
    private val randomEventGenerator = RandomEventGenerator(isRealRandom = isRealRandom, isDebug = false)

    /**
     * @param n = shape, в некоторых источниках обозначается k
     * @param mu = rate или μ, в некоторых источниках не мю, а лямбда λ
     * @return Случайная величина из распределения Эрланга
     */
    operator fun invoke(n: Int, mu: Double): Double {
        var multiplicationResult = 1.0
        val listU = List(n) { randomEventGenerator.invoke(Pa = 1.0).randomNumber }
        listU.map { multiplicationResult *= it }
        val result = -ln(multiplicationResult) / mu
        if (isDebug) {
            listU.forEachIndexed { index, d ->
                println("U${index + 1} = $d")
            }
            println("multiplicationResult = $multiplicationResult")
            println("Случайная величина из распределения Эрланга = $result")
        }
        return result
    }

    /**
     * Probability density function
     * @see <a href="https://en.wikipedia.org/wiki/Erlang_distribution"> erlang wiki </a>
     */
    fun calculateTheoretical(n: Int, mu: Double, x: Double): Double =
        (mu.pow(n) * x.pow(n - 1) * E.pow(-mu * x)) / factorial(n - 1)


    private fun factorial(num: Int) = if (num == 0) {
        1
    } else {
        (1..num).reduce { a, b -> a * b }
    }
}

private fun drawGraphs() {

    val erlangDistributionGenerator = ErlangDistributionGenerator(isRealRandom = true, isDebug = false)

    val n = 7
    val mu = 2.0
    val theoreticalXFrequency = 0.01
    val listTheoreticalX = List(1500) { it * theoreticalXFrequency }
    val listTheoreticalY = listTheoreticalX.map { erlangDistributionGenerator.calculateTheoretical(n, mu, it) }

    val randomValuesCount = 1000

    val dataTheoretical = mapOf<String, List<*>>(
        "x" to listTheoreticalX,
        "y" to listTheoreticalY
    )
    val pTheoretical = letsPlot(dataTheoretical) { x = "x"; y = "y" }

    val plotTheoretical = (pTheoretical +
            geomLine { y = "y" } +
            ggtitle(
                "Теоретическое распределение Эрланга",
                "при n = $n; μ = $mu; с шагом $theoreticalXFrequency"
            ) +
            //Среднее значение должно быть равно n/mu
            geomVLine(
                xintercept = (n/mu),
                color = "red",
                linetype = "dashed"
            ))

    val data = mapOf<String, List<Double>>(
        "x" to List(randomValuesCount) { erlangDistributionGenerator.invoke(n, mu) }
    )

    val p = letsPlot(data) { x = "x" }
    val plot = (p +
            //geomDensity() +
            geomHistogram(binWidth = 0.1, color="black", fill="white") { y = "..density.."} +
            geomDensity(linetype = 1) +
            ggtitle(
                "Фактическое распределение Эралнга",
                "при n = $n; μ = $mu; размером = $randomValuesCount"
            ) + geomVLine(xintercept = (data["x"] as List<Double>).average(), color = "red", linetype = "dashed"))

    GGBunch()
        .addPlot(
            plot, 0, 0, 500, 700
        )
        .addPlot(
            plotTheoretical, 600, 0, 500, 700
        )
        .show()
}
