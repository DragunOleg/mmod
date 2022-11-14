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
    }
    var button = JButton("Process")
    val label = JLabel("Задайте веса компонент вектора, кратные 16 с суммой 16 через пробел\n")

    val labelA = JLabel("Интервалы для вектора А:")
    var textFieldA = JTextField(ParamsSaver.loadIprOneParams().vectorAString, 40)

    val labelB = JLabel("Интервалы для вектора B:")
    var textFieldB = JTextField(ParamsSaver.loadIprOneParams().vectorBString, 40)

    val labelN = JLabel("Число случайных величин:")
    var textFieldRVN = JTextField(ParamsSaver.loadIprOneParams().RVN.toString(), 10)

    init {
        layout = FlowLayout()
        defaultCloseOperation = EXIT_ON_CLOSE
        setLocationRelativeTo(null)
        isVisible = true
        setSize(500, 200)

        textFieldA.addKeyListener(object : KeyAdapter() {
                override fun keyReleased(event: KeyEvent) {
                    val content = textFieldA.text
                    button.isEnabled = content != ""
                }
            })

        textFieldB.addKeyListener(object : KeyAdapter() {
            override fun keyReleased(event: KeyEvent) {
                val content = textFieldB.text
                button.isEnabled = content != ""
            }
        })

        //block non number for n
        textFieldRVN.addKeyListener(object : KeyAdapter() {
            override fun keyTyped(e: KeyEvent) {
                val c = e.keyChar
                if (!((c in '0'..'9') ||
                            c.code == KeyEvent.VK_BACK_SPACE
                            || c.code == KeyEvent.VK_DELETE)) {
                    toolkit.beep()
                    e.consume()
                }
            }
        })

        button.addActionListener {
            processButtonClick()
        }

        add(label)
        add(labelA)
        add(textFieldA)
        add(labelB)
        add(textFieldB)
        add(labelN)
        add(textFieldRVN)
        add(button)
    }

    private fun processButtonClick() {
        try {
            val vectorA = InputValidator.validateString(textFieldA.text)
            val n = vectorA.size.also { println("n = $it") }
            val vectorB = InputValidator.validateString(textFieldB.text)
            val m = vectorB.size.also { println("m = $it") }
            val RVN = textFieldRVN.text.toInt().also { println("число случайных величин = $it") }
            val probMatrix = excelReportGenerator.generateMatrix(vectorA, vectorB)
            // TODO: генерить RVN двухмерных величин так, чтобы удобно было обрабатывать результаты
            ParamsSaver.saveIprOneParams(IprOneParams(
                vectorAString = textFieldA.text,
                vectorBString = textFieldB.text,
                RVN = RVN
            ))
            JOptionPane.showMessageDialog(
                this@IprOneInputGetter,
                """
                    Результат процессинга:
                    $vectorA
                    $vectorB
                    """.trimIndent()
            )
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
