package kr_one

import org.jetbrains.letsPlot.GGBunch
import org.jetbrains.letsPlot.geom.geomBar
import org.jetbrains.letsPlot.geom.geomPoint
import org.jetbrains.letsPlot.intern.Plot
import org.jetbrains.letsPlot.label.ggtitle
import org.jetbrains.letsPlot.letsPlot
import java.awt.FlowLayout
import javax.swing.*

class ComplexDependantEventGenerator(
    val isRealRandom: Boolean,
    val isDebug: Boolean,
    val randomEventGenerator: RandomEventGenerator
) {

    operator fun invoke(Pa: Double, Pb: Double, PBdependantA: Double): Result {
        val resultA = randomEventGenerator(Pa)
        val resultB = randomEventGenerator(Pb)

        val PBdependant_notA = try {
            calculate_PBdepentant_notA(Pa, Pb, PBdependantA)
        } catch (e: Exception) {
            val exceptionResult =
                Result.InvalidData(
                    Pa,
                    Pb,
                    PBdependantA,
                    INVALID_PBDEPENDANT_NOTA,
                    resultA.randomNumber,
                    resultB.randomNumber
                )
            if (isDebug) println(exceptionResult.debugString())
            return exceptionResult
        }

        val complexDependantEventResult =
            if (resultA.result) { //x1<=Pa
                if (resultB.randomNumber <= PBdependantA) { //x2<=P(B/A)
                    Result.AB(Pa, Pb, PBdependantA, PBdependant_notA, resultA.randomNumber, resultB.randomNumber)
                } else { //x2>P(B/A)
                    Result.A_notB(Pa, Pb, PBdependantA, PBdependant_notA, resultA.randomNumber, resultB.randomNumber)
                }
            } else {  //x1>Pa
                if (resultB.randomNumber <= PBdependant_notA) { //x2<=P(B/!A)
                    Result.notA_B(Pa, Pb, PBdependantA, PBdependant_notA, resultA.randomNumber, resultB.randomNumber)
                } else { //x2>P(B/!A)
                    Result.notA_notB(Pa, Pb, PBdependantA, PBdependant_notA, resultA.randomNumber, resultB.randomNumber)
                }
            }
        if (isDebug) println(complexDependantEventResult.debugString())
        return complexDependantEventResult

    }

    /**
     * Возьмем за А - событие из RandomEventGenerator.
     * формула полной ветоятности:
     * P(B)=P(B/A)*P(A) +P(B/!A)*P(!A), отсюда выведем P(B/!A):
     *
     * P(B)-P(B/A)*P(A)
     * -------------- = P(B/!A)
     *      P(!A)
     *
     */
    @Throws(Exception::class)
    private fun calculate_PBdepentant_notA(Pa: Double, Pb: Double, PBdependantA: Double): Double {
        //если невозможно событие !A, то и P(B/!A) невозможно (+ деление на ноль)
        if (Pa == 1.0) {
            throw Exception("P(A) = 1.0")
        }
        //если невозможно событие B, то и зависимые события невозможны и задача не имеет смысла
        if (Pb == 0.0) {
            throw Exception("P(B) = 0.0")
        }
        //вероятность одной части уравнения не может быть больше P(B)
        if (PBdependantA * Pa > Pb) {
            throw Exception("P(B/A)*P(A) > P(B)")
        }

        val PBdependant_notA = (Pb - PBdependantA * Pa) / (1.0 - Pa)

        if (PBdependant_notA > 1.0 || PBdependant_notA < 0) {
            throw Exception("P(B/A) не валидно, данные не совместны")
        }

        return PBdependant_notA
    }

    sealed class Result(
        val Pa: Double,
        val Pb: Double,
        val PBdependant_A: Double,
        val PBdependant_notA: Double,
        val x1: Double,
        val x2: Double
    ) {
        /**
         * Деталь вышла с завода 1 и она хорошая
         */
        class AB(Pa: Double, Pb: Double, PBdependantA: Double, PBdependant_notA: Double, x1: Double, x2: Double) :
            Result(Pa, Pb, PBdependantA, PBdependant_notA, x1, x2)

        /**
         * Деталь вышла с завода 1 и она плохая
         */
        class A_notB(Pa: Double, Pb: Double, PBdependantA: Double, PBdependant_notA: Double, x1: Double, x2: Double) :
            Result(Pa, Pb, PBdependantA, PBdependant_notA, x1, x2)

        /**
         * Деталь вышла с завода 2 и она хорошая
         */
        class notA_B(Pa: Double, Pb: Double, PBdependantA: Double, PBdependant_notA: Double, x1: Double, x2: Double) :
            Result(Pa, Pb, PBdependantA, PBdependant_notA, x1, x2)

        /**
         * Деталь вышла с завода 2 и она плохая
         */
        class notA_notB(
            Pa: Double,
            Pb: Double,
            PBdependantA: Double,
            PBdependant_notA: Double,
            x1: Double,
            x2: Double
        ) :
            Result(Pa, Pb, PBdependantA, PBdependant_notA, x1, x2)

        class InvalidData(
            Pa: Double,
            Pb: Double,
            PBdependantA: Double,
            PBdependant_notA: Double = 0.0,
            x1: Double,
            x2: Double
        ) : Result(Pa, Pb, PBdependantA, PBdependant_notA, x1, x2)

        fun debugString(): String {
            return ("Pa = $Pa, Pb = $Pb, PBdependant_A = $PBdependant_A, PBdependant_notA = $PBdependant_notA"
                    //+ "\n ${this.javaClass.simpleName} : x1 = $x1, x2 = $x2"
                    )
        }

        fun graphTitle(): String = this.javaClass.simpleName
    }

    companion object {
        const val INVALID_PBDEPENDANT_NOTA = 10000.0
    }
}

class KrOne3InputGetter : JFrame("Кр1, 3") {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SwingUtilities.invokeLater { KrOne3InputGetter() }
        }
    }

    private val button = JButton("Process")
    private val label = JLabel("Задайте ")

    private val labelN = JLabel("n =")
    private val textFieldN = JTextField(KrOneParamsSaver.loadKrOneThreeParams().n.toString(), 5)

    private val labelPa = JLabel("Pa =")
    private val textFieldPa = JTextField(KrOneParamsSaver.loadKrOneThreeParams().Pa.toString(), 5)
    private val labelPb = JLabel("Pb =")
    private val textFieldPb = JTextField(KrOneParamsSaver.loadKrOneThreeParams().Pb.toString(), 5)
    private val labelPBdependantA = JLabel("P(b/a) =")
    private val textFieldPBdependantA = JTextField(KrOneParamsSaver.loadKrOneThreeParams().PBdependantA.toString(), 5)
    private val cbRandom = JCheckBox("isRealRandom").apply {
        isSelected = KrOneParamsSaver.loadKrOneThreeParams().realRandom
    }
    private val cbDebug = JCheckBox("debugLog").apply {
        isSelected = KrOneParamsSaver.loadKrOneThreeParams().debug
    }
    private val cbValues = JCheckBox("showValues").apply {
        isSelected = KrOneParamsSaver.loadKrOneThreeParams().valuesDraw
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
        add(labelPBdependantA)
        add(textFieldPBdependantA)
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
            val PBdependant_A = textFieldPBdependantA.text.toDouble()
            val isRealRandom = cbRandom.isSelected
            val isDebug = cbDebug.isSelected
            val isValuesDraw = cbValues.isSelected

            val randomEventGenerator = RandomEventGenerator(
                isRealRandom = isRealRandom, isDebug = isDebug
            )
            val complexDependantEventGenerator = ComplexDependantEventGenerator(
                isRealRandom = isRealRandom, isDebug = isDebug, randomEventGenerator = randomEventGenerator
            )

            drawGraphs(n, Pa, Pb, PBdependant_A, complexDependantEventGenerator, isValuesDraw)

            KrOneParamsSaver.saveKrOneThreeParams(
                KrOneThreeParams(
                    Pa = Pa,
                    Pb = Pb,
                    PBdependantA = PBdependant_A,
                    n = n,
                    realRandom = isRealRandom,
                    debug = isDebug,
                    valuesDraw = isValuesDraw
                )
            )
        } catch (e: Exception) {
            JOptionPane.showMessageDialog(
                this@KrOne3InputGetter, """
                    Ошибка во время процессинга:
                    ${e.message}
                    """.trimIndent()
            )
        }
    }

    private fun drawGraphs(
        n: Int,
        Pa: Double,
        Pb: Double,
        PBdependant_A: Double,
        complexDependantEventGenerator: ComplexDependantEventGenerator,
        valuesDraw: Boolean
    ) {
        //хороший пример для отчета
        //    val Pa = 0.5
        //    val Pb = 0.3
        //    val PBdependant_A = 0.5
        //тогда P(B) = P(A)*P(B/A) + P(!A)*P(B/!A)
        //0.3 = 0.5*0.5 + (1-0.5)*x
        //искомое = 0.1, тогда !A!B должно быть 0.9 от всех !A, или для десяти тысяч тысяч
        //4500 и 500 для искомого
        val resultList = List<ComplexDependantEventGenerator.Result>(n) {
            complexDependantEventGenerator.invoke(Pa, Pb, PBdependant_A)
        }
        println("Testing resultList with n = $n, Pa = $Pa, Pb = $Pb, P(B/A) = $PBdependant_A, calc P(B/!A) = ${resultList.first().PBdependant_notA}")

        val ABList = resultList.filterIsInstance<ComplexDependantEventGenerator.Result.AB>().apply {
            println("AB size: $size")
        }
        val notA_BList = resultList.filterIsInstance<ComplexDependantEventGenerator.Result.notA_B>().apply {
            println("notA_B size: $size")
        }
        val A_notBList = resultList.filterIsInstance<ComplexDependantEventGenerator.Result.A_notB>().apply {
            println("A_notB size: $size")
        }
        val notA_notBList = resultList.filterIsInstance<ComplexDependantEventGenerator.Result.notA_notB>().apply {
            println("notA_notB size: $size")
        }
        val invalidDataList = resultList.filterIsInstance<ComplexDependantEventGenerator.Result.InvalidData>().apply {
            println("Invalid size: $size")
        }
        val bunch = GGBunch()


        val data =
            mapOf<String, List<*>>("x" to ABList.map { "AB" } + notA_BList.map { "!AB" } + A_notBList.map { "A!B" } + notA_notBList.map { "!A!B" } + invalidDataList.map { "invalid" })
        val p = letsPlot(data) { x = "x" } + ggtitle(
            "Фактическое распределение при n = $n; Pa = $Pa, Pb = $Pb",
            "P(B/A) = $PBdependant_A, calc P(B/!A) = ${resultList.first().PBdependant_notA}"
        )

        val ABt = List((n*Pa*PBdependant_A).toInt()) {"AB"}
        val A_notBt = List((n*Pa*(1.0-PBdependant_A)).toInt()) { "A!B"}
        val notA_Bt = List((n * (1.0-Pa) * resultList.first().PBdependant_notA).toInt()) { "!AB"}
        val notA_notBt = List((n * (1.0-Pa) * (1.0-resultList.first().PBdependant_notA)).toInt()) { "!A!B"}
        val data2 =
            mapOf<String, List<*>>("x" to ABt + notA_Bt + A_notBt + notA_notBt)
        val p2 = letsPlot(data2) {x = "x"} + ggtitle ("Теоретическое", "")

        bunch.addPlot(p+geomBar(), 0, 0, 590, 390)
        bunch.addPlot(p2+geomBar(), 600, 0, 390, 390)
        bunch.show()
        if (valuesDraw) {
            drawValues(resultList)
        }
    }
}

private fun drawValues(
    resultList: List<ComplexDependantEventGenerator.Result>
) {
    val ABList = resultList.filterIsInstance<ComplexDependantEventGenerator.Result.AB>()
    val notA_BList = resultList.filterIsInstance<ComplexDependantEventGenerator.Result.notA_B>()
    val A_notBList = resultList.filterIsInstance<ComplexDependantEventGenerator.Result.A_notB>()
    val notA_notBList = resultList.filterIsInstance<ComplexDependantEventGenerator.Result.notA_notB>()
    val invalidDataList = resultList.filterIsInstance<ComplexDependantEventGenerator.Result.InvalidData>()

    GGBunch()
        .addPlot(
            plotList(ABList), 0, 350, 300, 300
        )
        .addPlot(
            plotList(notA_BList), 350, 350, 300, 300
        )
        .addPlot(
            plotList(A_notBList), 0, 0, 300, 300
        )
        .addPlot(
            plotList(notA_notBList), 350, 0, 300, 300
        )
        .addPlot(
            plotList(invalidDataList), 700, 0, 300, 300
        )
        .show()
}

private fun plotList(list: List<ComplexDependantEventGenerator.Result>): Plot {
    val data = mapOf<String, List<*>>(
        "x1" to list.map { it.x1 },
        "x2" to list.map { it.x2 }
    )

    val p = letsPlot(data) { x = "x1"; y = "x2" } + ggtitle(list.firstOrNull()?.graphTitle() + " : ${list.size}")
    return (p + geomPoint(shape = 4))
}
