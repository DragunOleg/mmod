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
    }
    var button = JButton("Process")
    val label = JLabel("Задайте веса компонент вектора, кратные 16 с суммой 16 через пробел")

    val labelA = JLabel("Задайте фоматированный вектор А:")
    var textFieldA = JTextField(ParamsSaver.loadIprOneParams().vectorAString, 40)

    val labelB = JLabel("Задайте форматированный вектор B:")
    var textFieldB = JTextField(ParamsSaver.loadIprOneParams().vectorBString, 40)

    init {
        layout = FlowLayout()
        defaultCloseOperation = EXIT_ON_CLOSE
        setLocationRelativeTo(null)
        isVisible = true
        setSize(500, 200)

        // adds key event listener
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

        // adds action listener for the button
        button.addActionListener {
            processButtonClick()
        }

        add(label)
        add(labelA)
        add(textFieldA)
        add(labelB)
        add(textFieldB)
        add(button)
    }

    private fun processButtonClick() {
        try {
            val vectorA = InputValidator.validateString(textFieldA.text)
            val vectorB = InputValidator.validateString(textFieldB.text)
            ParamsSaver.saveIprOneParams(IprOneParams(
                vectorAString = textFieldA.text,
                vectorBString = textFieldB.text
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
