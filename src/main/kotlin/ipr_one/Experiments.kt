package ipr_one

import java.awt.Color
import java.awt.FlowLayout
import java.awt.Font
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.*

/**
 * This program demonstrates various techniques when using JTextField
 * @author www.codejava.net
 */
class SwingJTextFieldDemo : JFrame("Demo program for JTextField") {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SwingUtilities.invokeLater { SwingJTextFieldDemo() }
        }
    }

    var textField = JTextField("This is some text", 20)
    var button = JButton("OK")

    init {
        layout = FlowLayout()

        // customizes appearance: font, foreground, background
        textField.font = Font("Arial", Font.ITALIC or Font.BOLD, 12)
        textField.foreground = Color.BLUE
        textField.background = Color.YELLOW

        // customizes text selection
        textField.selectionColor = Color.CYAN
        textField.selectedTextColor = Color.RED

        // sets initial selection
        textField.selectionStart = 8
        textField.selectionEnd = 12

        // adds event listener which listens to Enter key event
        textField.addActionListener {
            JOptionPane.showMessageDialog(
                this@SwingJTextFieldDemo,
                """
                    You entered text:
                    ${textField.text}
                    """.trimIndent()
            )
        }

        // adds key event listener
        textField.addKeyListener(object : KeyAdapter() {
            override fun keyReleased(event: KeyEvent) {
                val content = textField.text
                button.isEnabled = content != ""
            }
        })

        // adds action listener for the button
        button.addActionListener {
            JOptionPane.showMessageDialog(
                this@SwingJTextFieldDemo,
                """
                    Content of the text field:
                    ${textField.text}
                    """.trimIndent()
            )
        }
        add(textField)
        add(button)
        setSize(300, 100)
        defaultCloseOperation = EXIT_ON_CLOSE
        setLocationRelativeTo(null)
        isVisible = true
    }
}
