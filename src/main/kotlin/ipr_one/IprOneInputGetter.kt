package ipr_one

import java.awt.FlowLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.*

class IprOneInputGetter : JFrame("VectorsGetter") {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SwingUtilities.invokeLater { IprOneInputGetter() }
        }
        val excelReportGenerator = ExcelReportGenerator()
        val random2DValueGenerator = Random2DValueGenerator(isRealRandom = true, isDebug = false)
    }
    var button = JButton("Process")
    val label = JLabel("Задайте целочисленные значения")

    val labelN = JLabel("n =")
    val textFieldN = JTextField(ParamsSaver.loadIprOneParams().n.toString(), 4)

    val labelM = JLabel("m =")
    val textFieldM = JTextField(ParamsSaver.loadIprOneParams().m.toString(), 4)

    val labelMatrix = JLabel("Веса матрицы =")
    val textFieldMatrix = JTextField(ParamsSaver.loadIprOneParams().matrix, 23)

    val labelA = JLabel("Значения вектора А =")
    var textFieldA = JTextField(ParamsSaver.loadIprOneParams().vectorAString, 20)

    val labelB = JLabel("Значения вектора B= ")
    var textFieldB = JTextField(ParamsSaver.loadIprOneParams().vectorBString, 20)

    val labelRVN = JLabel("Число случайных величин:")
    var textFieldRVN = JTextField(ParamsSaver.loadIprOneParams().RVN.toString(), 10)

    init {
        layout = FlowLayout()
        defaultCloseOperation = EXIT_ON_CLOSE
        setLocationRelativeTo(null)
        isVisible = true
        setSize(420, 200)

        val keyListener = object : KeyAdapter() {
            override fun keyTyped(e: KeyEvent) {
                val c = e.keyChar
                if (!((c in '0'..'9')
                            || c.code == KeyEvent.VK_BACK_SPACE
                            || c.code == KeyEvent.VK_SPACE
                            || c.code == KeyEvent.VK_DELETE)) {
                    toolkit.beep()
                    e.consume()
                }
            }
        }
        textFieldN.addKeyListener(keyListener)
        textFieldM.addKeyListener(keyListener)
        textFieldMatrix.addKeyListener(keyListener)
        textFieldA.addKeyListener(keyListener)
        textFieldB.addKeyListener(keyListener)
        textFieldRVN.addKeyListener(keyListener)

        button.addActionListener {
            processButtonClick()
        }

        add(label)
        add(labelN)
        add(textFieldN)
        add(labelM)
        add(textFieldM)
        add(labelMatrix)
        add(textFieldMatrix)
        add(labelA)
        add(textFieldA)
        add(labelB)
        add(textFieldB)
        add(labelRVN)
        add(textFieldRVN)
        add(button)
    }

    private fun processButtonClick() {
        try {
            val n = textFieldN.text.toInt().also { println("n = $it") }
            val m = textFieldM.text.toInt().also { println("m = $it") }
            val matrixString = textFieldMatrix.text.also { println("matrix string = $it") }
            val vectorAString = textFieldA.text.also { println("Vector A = $it") }
            val vectorBString = textFieldB.text.also { println("Vector B = $it") }
            val RVN = textFieldRVN.text.toInt().also { println("число случайных величин = $it") }
            val vectorA = vectorAString.trim().split(" ").map { it.toInt() }
            val vectorB = vectorBString.trim().split(" ").map { it.toInt() }
            InputValidator.validateInput(n, m, matrixString, vectorAString, vectorBString, RVN)
            val probMatrix = excelReportGenerator.generateProbabilityMatrix(n, m, matrixString, vectorA, vectorB)
            val listToAnalyze = random2DValueGenerator.invoke(
                probMatrix = probMatrix,
                vectorA = vectorA,
                vectorB = vectorB,
                RVN = RVN
            )
            val empiricalMatrix = excelReportGenerator.generateEmpiricalDistributionMatrix(
                n = n,
                m = m,
                list = listToAnalyze,
                vectorA = vectorA,
                vectorB = vectorB
            )
            random2DValueGenerator.drawVectorsHist(listToAnalyze, empiricalMatrix, vectorA, vectorB)
            ParamsSaver.saveIprOneParams(IprOneParams(
                n = n,
                m = m,
                matrix = matrixString,
                vectorAString = vectorAString,
                vectorBString = vectorBString,
                RVN = RVN
            ))
        } catch (e:Exception) {
            JOptionPane.showMessageDialog(
                this@IprOneInputGetter,
                """
                    Ошибка во время процессинга:
                    ${e.message}
                    """.trimIndent()
            )
        }
    }
}
