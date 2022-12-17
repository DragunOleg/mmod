package ipt_two

import java.awt.FlowLayout
import java.lang.Exception
import javax.swing.*

class IprTwoInputGetter : JFrame("IPR2") {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            //remove annoying warning "Graphics2D from BufferedImage lacks BUFFERED_IMAGE hint", was actual for 1/2 PC
            System.setProperty("org.apache.batik.warn_destination", "false")

            SwingUtilities.invokeLater { IprTwoInputGetter() }
        }
    }

    var startButton = JButton("start")
    var stopButton = JButton("stop")

    val labelChannelsN = JLabel("n каналов =")
    val textFieldChannelsN = JTextField(IprTwoParamsSaver.loadIprTwoParams().channelsN.toString(), 4)

    val labelQueueM = JLabel("m мест в очереди =")
    val textFieldQueueN = JTextField(IprTwoParamsSaver.loadIprTwoParams().queueM.toString(), 4)

    val labelInputFlowLambda = JLabel("λ интенсивность входящего потока? =")
    val textFieldInputFlowLambda = JTextField(IprTwoParamsSaver.loadIprTwoParams().inputFlowLambda.toString(), 4)

    val labelServiceFlowMu = JLabel("μ интенсивность потока обслуживания? =")
    val textFieldServiceFlowMu = JTextField(IprTwoParamsSaver.loadIprTwoParams().serviceFlowMu.toString(), 4)

    val labelLeavingNu = JLabel("ν параметр закона ухода =")
    val textFieldLeavingNu = JTextField(IprTwoParamsSaver.loadIprTwoParams().leavingNu.toString(), 4)

    var nChannels: Int? = null
    var mQueue: Int? = null
    var lambdaInputFlow: Double? = null
    var muServiceFlow: Double? = null
    var nuLeaving: Double? = null


    init {
        layout = FlowLayout()
        defaultCloseOperation = EXIT_ON_CLOSE
        setLocationRelativeTo(null)
        isVisible = true
        setSize(420, 200)

        startButton.addActionListener {
            startButtonClicked()
        }
        stopButton.addActionListener {
            stopButtonClicked()
        }

        add(labelChannelsN)
        add(textFieldChannelsN)
        add(labelQueueM)
        add(textFieldQueueN)
        add(labelInputFlowLambda)
        add(textFieldInputFlowLambda)
        add(labelServiceFlowMu)
        add(textFieldServiceFlowMu)
        add(labelLeavingNu)
        add(textFieldLeavingNu)
        add(startButton)
        add(stopButton)

        // TODO: enable and calculate theoretical without issues 
        labelLeavingNu.disable()
        textFieldLeavingNu.disable()
    }

    private fun startButtonClicked() {
        try {
            nChannels = textFieldChannelsN.text.toInt().also { println("n каналов = $it") }
            mQueue = textFieldQueueN.text.toInt().also { println("m место в очереди = $it") }
            lambdaInputFlow = textFieldInputFlowLambda.text.toDouble().also { println("λ вход = $it") }
            muServiceFlow = textFieldServiceFlowMu.text.toDouble().also { println("μ обслуживания = $it") }
            nuLeaving = textFieldLeavingNu.text.toDouble().also { println("ν параметр закона ухода = $it") }

        } catch (e: Exception) {
            JOptionPane.showMessageDialog(
                this@IprTwoInputGetter,
                """
                    Ошибка во время работы системы:
                    ${e.message}
                    """.trimIndent()
            )
        }
    }

    private fun stopButtonClicked() {
        try {

            IprTwoParamsSaver.saveIprTwoParams(
                IprTwoParams(
                channelsN = nChannels!!,
                queueM = mQueue!!,
                inputFlowLambda = lambdaInputFlow!!,
                serviceFlowMu = muServiceFlow!!,
                leavingNu = nuLeaving!!
            )
            )

        } catch (e: Exception) {
            JOptionPane.showMessageDialog(
                this@IprTwoInputGetter,
                """
                    Ошибка во время обработки результатов:
                    ${e.message}
                    """.trimIndent()
            )
        }
    }
}