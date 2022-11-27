package kr_one

import org.jetbrains.letsPlot.GGBunch
import org.jetbrains.letsPlot.geom.geomBar
import org.jetbrains.letsPlot.geom.geomHistogram
import org.jetbrains.letsPlot.geom.geomVLine
import org.jetbrains.letsPlot.label.ggtitle
import org.jetbrains.letsPlot.letsPlot
import java.awt.FlowLayout
import javax.swing.*
import kotlin.math.pow
import kotlin.math.sqrt

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


class KrOne1InputGetter : JFrame("Кр 1, 1") {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SwingUtilities.invokeLater { KrOne1InputGetter() }
        }

    }

    private val button = JButton("Process")
    private val label = JLabel("Задайте Pa, и несколько n через пробел   ")

    private val labelN = JLabel("n =")
    private val textFieldN = JTextField(KrOneParamsSaver.loadKrOneOneParams().n,21)

    private val labelPa = JLabel("Pa =")
    private val textFieldPa = JTextField(KrOneParamsSaver.loadKrOneOneParams().Pa.toString(),20)
    private val cbRandom = JCheckBox("isRealRandom").apply {
        isSelected = KrOneParamsSaver.loadKrOneOneParams().realRandom
    }
    private val cbDebug = JCheckBox("debugLog").apply {
        isSelected = KrOneParamsSaver.loadKrOneOneParams().debug
    }

    init {
        layout = FlowLayout()
        defaultCloseOperation = EXIT_ON_CLOSE
        setLocationRelativeTo(null)
        isVisible = true
        setSize(295, 200)

        button.addActionListener {
            processButtonClick()
        }

        add(label)
        add(labelPa)
        add(textFieldPa)
        add(labelN)
        add(textFieldN)
        add(cbRandom)
        add(cbDebug)
        add(button)
    }

    private fun processButtonClick() {
        try {
            val n = textFieldN.text
            val sizes = n.trim().split(" ").map { it.toInt() }
            val Pa = textFieldPa.text.toDouble()
            val isRealRandom = cbRandom.isSelected
            val isDebug = cbDebug.isSelected
            val randomEventGenerator = RandomEventGenerator(
                isRealRandom = isRealRandom,
                isDebug = isDebug
            )
            drawGraphs(Pa, sizes, randomEventGenerator)
            KrOneParamsSaver.saveKrOneOneParams(
                KrOneOneParams(
                    Pa = Pa,
                    n = n,
                    realRandom = isRealRandom,
                    debug = isDebug
                )
            )
        } catch (e: Exception) {
            JOptionPane.showMessageDialog(
                this@KrOne1InputGetter,
                """
                    Ошибка во время процессинга:
                    ${e.message}
                    """.trimIndent()
            )
        }
    }

    private fun drawGraphs(Pa: Double, sizes: List<Int>, randomEventGenerator: RandomEventGenerator) {
        val bunch = GGBunch()
        sizes.forEachIndexed { index, n ->
            val list = List(n) { randomEventGenerator.invoke(Pa) }
            val data = mapOf<String, List<Double>>(
                "x" to list.map { it.randomNumber }
            )
            val mx = data["x"]!!.average()
            var dxSum = 0.0
            data["x"]!!.forEach {
                dxSum += (it - mx).pow(2)
            }
            //Несмещенная состоятельная оценка дисперсии
            val dx = dxSum / (n - 1)

            //Состоятельная оценка среднеквадратичного отклонения
            val sigma = sqrt(dx)
            val p = letsPlot(data) { x = "x" } + ggtitle(
                "Pa = $Pa, n=$n\n" +
                        "Выборочное средее:\n" +
                        "$mx\n" +
                        "D[X] = $dx\n" +
                        "σ = $sigma"
            )
            println(
                "Выборочное средее,n=$n:\n" +
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
                ), index * 300, 0, 290, 190
            )
            val data2 = mapOf<String, List<Double>>(
                "x" to list.map {
                    if (it.result) {
                        0.0
                    } else {
                        1.0
                    }
                }
            )
            val p2 = letsPlot(data2) { x = "x" } + ggtitle("0 = A; 1 = !A")
            bunch.addPlot(

                p2 + geomBar(),
                index * 300, 200, 290, 190
            )
            val PaNInt = (Pa * n).toInt()
            val data3 = mapOf<String, List<Double>>(
                "x" to List(PaNInt) { 0.0 } + List(n-PaNInt) { 1.0 }
            )
            val p3 = letsPlot(data3) { x = "x" } + ggtitle("Theoretical:")
            bunch.addPlot(

                p3 + geomBar(),
                index * 300, 400, 290, 190
            )
        }
        bunch.show()
    }
}
