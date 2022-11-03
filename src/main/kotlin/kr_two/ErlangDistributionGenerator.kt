package kr_two

import kr_one.RandomEventGenerator
import org.jetbrains.letsPlot.GGBunch
import org.jetbrains.letsPlot.geom.geomDensity
import org.jetbrains.letsPlot.geom.geomHistogram
import org.jetbrains.letsPlot.geom.geomLine
import org.jetbrains.letsPlot.geom.geomVLine
import org.jetbrains.letsPlot.label.ggtitle
import org.jetbrains.letsPlot.letsPlot
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.*

//todo тесты класса
//todo посчитать кси-квадрат https://wiki.loginom.ru/articles/chi-square-test.html
fun main() {
    //remove annoying warning "Graphics2D from BufferedImage lacks BUFFERED_IMAGE hint", was actual for 1/2 PC
    System.setProperty("org.apache.batik.warn_destination", "false")

    val erlangDistributionGenerator = ErlangDistributionGenerator(isRealRandom = true, isDebug = true)
    val n = 7
    val mu = 2.0
    repeat(3) {
        erlangDistributionGenerator(n = n, mu = mu)
    }

    drawGraphs(n, mu)
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

private fun drawGraphs(n: Int, mu: Double) {

    val erlangDistributionGenerator = ErlangDistributionGenerator(isRealRandom = true, isDebug = false)

    val theoreticalXFrequency = 0.01
    val listTheoreticalX = List(1500) { it * theoreticalXFrequency }
    val listTheoreticalY = listTheoreticalX
        .map { erlangDistributionGenerator.calculateTheoretical(n, mu, it) }

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
                xintercept = (n / mu),
                color = "red",
                linetype = "dashed"
            ))

    val data = mapOf<String, List<Double>>(
        "x" to List(randomValuesCount) { erlangDistributionGenerator.invoke(n, mu) }
    )

    val p = letsPlot(data) { x = "x" }
    val plot = (p +
            geomHistogram(binWidth = 0.1, color = "black", fill = "white") { y = "..density.." } +
            //default adjust is 1
            geomDensity(linetype = 1, adjust = 0.1) +
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

    /**
     * ~~~~~~~~~~~~~~~~~~~~~~Точечные оценки~~~~~~~~~~~~~~~~~~~~~~~~~~
     */
    val mx = n / mu
    println("Мат.ожидание = $mx")
    val dx = data["x"]!!.average()
    println("Несмещенная состоятельная оценка математического ожидания или выборочное среднее= $dx")
    val s02 = 1.0 / (randomValuesCount - 1) * data["x"]!!.fold(0.0) { sum, element -> sum + (element - dx).pow(2) }
    println("Несмещенная состоятельная оценка дисперсии =  $s02")
    val s2 = 1.0/ randomValuesCount * data["x"]!!.fold(0.0) { sum, element -> sum + (element - dx).pow(2) }
    println("Смещенная состоятельная оценка дисперсии = $s2")
    val s12 = 1.0 / randomValuesCount * data["x"]!!.fold(0.0) { sum, element -> sum + (element - mx).pow(2) }
    println("Несмещенная состоятельная оценка дисперсии = $s12")
    val s0 = sqrt(s02)
    println("Состоятельная оценка среднеквадратичного отклонения = $s0")
    repeat(5) { k ->
        val ak = 1.0 / randomValuesCount * data["x"]!!.fold(0.0) { sum, element -> sum + element.pow(k+1) }
        val muk = 1.0 / randomValuesCount * data["x"]!!.fold(0.0) { sum, element -> sum + (element - dx).pow(k+1) }
        println("Выборочный началный момент ${k+1}-го порядка: $ak")
        println("Выборочный центральный момент ${k+1}-го порядка: $muk")
    }
    /**
     * Метод моментов
     */
    val lambdaMoments = dx / s02
    println("По методу моментов разделим оценку математического ожидания на оценку дисперсии, получим")
    println("mu` = $lambdaMoments")
    /**
     * ~~~~~~~~~~~~~~~~~~~~~~Интервальные оценки~~~~~~~~~~~~~~~~~~~~~~~~~~
     */
    //стъюдент до 1000 http://old.exponenta.ru/educat/referat/XIkonkurs/student5/tabt-st.pdf
    //для 99 это 2.6264055
    // для 999 это 2.5807596
    val left = dx - (s02 * 2.5807596)/ sqrt(randomValuesCount - 1.0)
    val right = dx + (s02 * 2.5807596)/ sqrt(randomValuesCount - 1.0)
    println("Доверительный интервал для математического ожидания при выборке размером 1000 и уровне значимости 0.99: \n" +
            "$left <= $mx <= $right")

    /**
     * ~~~~~~~~~~~~~~~~~~~~~~Соответствие закона распределения распределению Эрланга~~~~~~~~~~~~~~~~~~~~~~~~~~
     */
    //todo Критерий Колмогорова, страница 22
    val roundedDataX: List<Double> = (plot.data!!["x"] as List<Double>)
        //.map { it.roundToInt().toDouble() }
        .map { BigDecimal(it).setScale(1, RoundingMode.HALF_EVEN).toDouble() } //округляем до 1 знака после запятой
    //Сюда будем сплюсовывать density при каждом совпадении
    val calculatedDensity = listTheoreticalY.map { 0.0 }.toMutableList()

    roundedDataX.forEachIndexed { index, d ->
        //умножаем на 10 для выравнивания с theoreticalY
        calculatedDensity[listTheoreticalX.closestIndex(d)] += 1.0 / randomValuesCount * 10.0
    }

    val dataWeird = mapOf<String, List<*>>(
        "x" to listTheoreticalX,
        "y" to calculatedDensity
    )
    val pWeird = letsPlot(dataWeird) { x = "x"; y = "y" }

    val plotWeird = (pWeird +
            geomLine { y = "y" }).show()
    //calculatedDensity = значения, которые осталось сравнить с теоретическим

    var biggestViolation = 0 to 0.0
    calculatedDensity.forEachIndexed { index, d ->
        if (d != 0.0) {
            val indexDiff = abs(d - listTheoreticalY[index])
            if (indexDiff >= biggestViolation.second) {
                biggestViolation = index to indexDiff
            }
        }
    }
    //выведем самое большое несовпадение
    println("biggest violation index = ${biggestViolation.first} diff = ${biggestViolation.second}")
    println("теоретическое значение в этом индексе x = ${listTheoreticalX[biggestViolation.first]} y = ${listTheoreticalY[biggestViolation.first]}")
    println("экспериментальное значение после округлений = ${calculatedDensity[biggestViolation.first]}")
    //теперь посчитаем полный хи квадат
    var sum = 0.0
    var M = 0
    calculatedDensity.forEachIndexed { index, d ->
        if (d != 0.0 && listTheoreticalY[index] != 0.0) {
            sum += (listTheoreticalY[index] - d).pow(2) / listTheoreticalY[index]
            M+=1
        }
    }
    val xi2 = sum *randomValuesCount
    //todo критерий кси квадрат, страница 19
    //todo как определить число степеней свободы?
    println("sum = $sum")
    println("xi2 = $xi2")
}

fun List<Double>.closestIndex(value: Double): Int {
    val closest = minBy { abs(value - it) }
    val result = this.indexOf(closest)
    return result
}
