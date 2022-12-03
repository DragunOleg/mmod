package kr_one

import org.jetbrains.letsPlot.GGBunch
import org.jetbrains.letsPlot.geom.geomBar
import org.jetbrains.letsPlot.geom.geomPoint
import org.jetbrains.letsPlot.intern.Plot
import org.jetbrains.letsPlot.label.ggtitle
import org.jetbrains.letsPlot.letsPlot
import java.awt.FlowLayout
import javax.swing.*

class ComplexEventGenerator(
    val isRealRandom: Boolean, val isDebug: Boolean
) {
    private val randomEventGenerator = RandomEventGenerator(isRealRandom = isRealRandom, isDebug = false)

    operator fun invoke(Pa: Double, Pb: Double): Result {
        val resultA = randomEventGenerator.invoke(Pa)
        val resultB = randomEventGenerator.invoke(Pb)

        val complexEventResult = if (resultA.result) {
            if (resultB.result) {
                Result.AB(Pa = Pa, x1 = resultA.randomNumber, Pb = Pb, x2 = resultB.randomNumber)
            } else {
                Result.A_notB(Pa = Pa, x1 = resultA.randomNumber, Pb = Pb, x2 = resultB.randomNumber)
            }
        } else {
            if (resultB.result) {
                Result.notA_B(Pa = Pa, x1 = resultA.randomNumber, Pb = Pb, x2 = resultB.randomNumber)
            } else {
                Result.notA_notB(Pa = Pa, x1 = resultA.randomNumber, Pb = Pb, x2 = resultB.randomNumber)
            }
        }
        if (isDebug) println(complexEventResult.debugString())
        return complexEventResult
    }

    sealed class Result(
        val Pa: Double, val x1: Double, val Pb: Double, val x2: Double
    ) {
        class AB(Pa: Double, x1: Double, Pb: Double, x2: Double) : Result(Pa, x1, Pb, x2)
        class A_notB(Pa: Double, x1: Double, Pb: Double, x2: Double) : Result(Pa, x1, Pb, x2)
        class notA_B(Pa: Double, x1: Double, Pb: Double, x2: Double) : Result(Pa, x1, Pb, x2)
        class notA_notB(Pa: Double, x1: Double, Pb: Double, x2: Double) : Result(Pa, x1, Pb, x2)

        fun debugString(): String {
            return ("${this.javaClass.simpleName} :" + "Pa = $Pa, x1 = $x1, Pb = $Pb, x2 = $x2")
        }

        fun graphTitle(): String = this.javaClass.simpleName
    }
}

class KrOne2InputGetter : JFrame("Кр1, 2") {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SwingUtilities.invokeLater { KrOne2InputGetter() }
        }
    }

    private val button = JButton("Process")
    private val label = JLabel("Задайте ")

    private val labelN = JLabel("n =")
    private val textFieldN = JTextField(KrOneParamsSaver.loadKrOneTwoParams().n.toString(), 5)

    private val labelPa = JLabel("Pa =")
    private val textFieldPa = JTextField(KrOneParamsSaver.loadKrOneTwoParams().Pa.toString(), 5)
    private val labelPb = JLabel("Pb =")
    private val textFieldPb = JTextField(KrOneParamsSaver.loadKrOneTwoParams().Pb.toString(), 5)
    private val cbRandom = JCheckBox("isRealRandom").apply {
        isSelected = KrOneParamsSaver.loadKrOneTwoParams().realRandom
    }
    private val cbDebug = JCheckBox("debugLog").apply {
        isSelected = KrOneParamsSaver.loadKrOneTwoParams().debug
    }
    private val cbValues = JCheckBox("showValues").apply {
        isSelected = KrOneParamsSaver.loadKrOneTwoParams().valuesDraw
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
        add(labelPb)
        add(textFieldPb)
        add(labelN)
        add(textFieldN)
        add(cbRandom)
        add(cbDebug)
        add(cbValues)
        add(button)
    }


    private fun processButtonClick() {
        try {
            val n = textFieldN.text.toInt()
            val Pa = textFieldPa.text.toDouble()
            val Pb = textFieldPb.text.toDouble()
            val isRealRandom = cbRandom.isSelected
            val isDebug = cbDebug.isSelected
            val isValuesDraw = cbValues.isSelected

            val complexEventGenerator = ComplexEventGenerator(
                isRealRandom = isRealRandom, isDebug = isDebug
            )
            drawGraphs(n, Pa, Pb, complexEventGenerator, isValuesDraw)

            KrOneParamsSaver.saveKrOneTwoParams(
                KrOneTwoParams(
                    Pa = Pa, Pb = Pb, n = n, realRandom = isRealRandom, debug = isDebug, valuesDraw = isValuesDraw
                )
            )


        } catch (e: Exception) {
            JOptionPane.showMessageDialog(
                this@KrOne2InputGetter, """
                    Ошибка во время процессинга:
                    ${e.message}
                    """.trimIndent()
            )
        }
    }

    private fun drawGraphs(
        n: Int, Pa: Double, Pb: Double, complexEventGenerator: ComplexEventGenerator, valuesDraw: Boolean
    ) {
        val resultList = List<ComplexEventGenerator.Result>(n) { complexEventGenerator.invoke(Pa, Pb) }
        val ABList = resultList.filterIsInstance<ComplexEventGenerator.Result.AB>().apply {
            println("AB size = $size")
        }
        val notA_BList = resultList.filterIsInstance<ComplexEventGenerator.Result.notA_B>().apply {
            println("notA_B size = $size")
        }
        val A_notBList = resultList.filterIsInstance<ComplexEventGenerator.Result.A_notB>().apply {
            println("A_notB size = $size")
        }
        val notA_notBList = resultList.filterIsInstance<ComplexEventGenerator.Result.notA_notB>().apply {
            println("notA_notB size = $size")
        }
        val data =
            mapOf<String, List<*>>("x" to ABList.map { "AB" } + notA_BList.map { "!AB" } + A_notBList.map { "A!B" } + notA_notBList.map { "!A!B" })

        val p = letsPlot(data) { x = "x" } + ggtitle("Фактическое распределение при n = $n; Pa = $Pa, Pb = $Pb")
        (p + geomBar()).show()

        if (valuesDraw) {
            drawValues(n, Pa, Pb, resultList)
        }

    }
}


private fun drawValues(n: Int, Pa: Double, Pb: Double, resultList: List<ComplexEventGenerator.Result>) {
    println("Testing resultList with Pa = $Pa, Pb = $Pb, n = $n")
    val ABList = resultList.filterIsInstance<ComplexEventGenerator.Result.AB>()
    val notA_BList = resultList.filterIsInstance<ComplexEventGenerator.Result.notA_B>()
    val A_notBList = resultList.filterIsInstance<ComplexEventGenerator.Result.A_notB>()
    val notA_notBList = resultList.filterIsInstance<ComplexEventGenerator.Result.notA_notB>()
    GGBunch().addPlot(
            plotList(ABList), 0, 350, 300, 300
        ).addPlot(
            plotList(notA_BList), 350, 350, 300, 300
        ).addPlot(
            plotList(A_notBList), 0, 0, 300, 300
        ).addPlot(
            plotList(notA_notBList), 350, 0, 300, 300
        ).show()
}

private fun plotList(list: List<ComplexEventGenerator.Result>): Plot {
    val data = mapOf<String, List<*>>("x1" to list.map { it.x1 }, "x2" to list.map { it.x2 })

    val p = letsPlot(data) { x = "x1"; y = "x2" } + ggtitle(
        list.firstOrNull()?.graphTitle() + " : ${list.size}"
    )
    return (p + geomPoint(shape = 4))
}