package ipt_two

import kotlinx.coroutines.*
import java.awt.FlowLayout
import java.awt.event.ActionListener
import java.lang.Exception
import javax.swing.*
import kotlin.coroutines.CoroutineContext

class IprTwoInputGetter : JFrame("IPR2"), CoroutineScope {

    val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

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

    val labelInputFlowLambda = JLabel("λ интенсивность входящего потока =")
    val textFieldInputFlowLambda = JTextField(IprTwoParamsSaver.loadIprTwoParams().inputFlowLambda.toString(), 4)

    val labelServiceFlowMu = JLabel("μ интенсивность потока обслуживания =")
    val textFieldServiceFlowMu = JTextField(IprTwoParamsSaver.loadIprTwoParams().serviceFlowMu.toString(), 4)

    val labelLeavingNu = JLabel("ν параметр закона ухода =")
    val textFieldLeavingNu = JTextField(IprTwoParamsSaver.loadIprTwoParams().leavingNu.toString(), 4)

    var nChannels: Int? = null
    var mQueue: Int? = null
    var lambdaInputFlow: Double? = null
    var muServiceFlow: Double? = null
    var nuLeaving: Double? = null

    lateinit var stateCollector: StateCollector
    lateinit var smo: SMO
    lateinit var queueSMO: QueueSMO
    lateinit var requestProducer: RequestProducer
    var epochStartTime: Long = 0L

    fun getLeftQueueSize() = queueSMO.leftSize()
    fun getRequestProducedSize() = requestProducer.getRequestProducedSize()
    fun getAllFinishedList() = smo.getAllFinishedList()

    init {
        layout = FlowLayout()
        defaultCloseOperation = EXIT_ON_CLOSE
        setLocationRelativeTo(null)
        isVisible = true
        setSize(420, 200)

        startButton.addActionListener {
            startButtonClicked()
        }
        stopButton.isEnabled = false

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
            mQueue = textFieldQueueN.text.toInt().also { println("m мест в очереди = $it") }
            lambdaInputFlow = textFieldInputFlowLambda.text.toDouble().also { println("λ вход = $it") }
            muServiceFlow = textFieldServiceFlowMu.text.toDouble().also { println("μ обслуживания = $it") }
            nuLeaving = textFieldLeavingNu.text.toDouble().also { println("ν параметр закона ухода = $it") }


            launch {
                epochStartTime = System.currentTimeMillis()
                stateCollector = StateCollector(epochStartTime)
                queueSMO = QueueSMO(
                    capacityM = mQueue!!,
                    epochTime = epochStartTime,
                    reportSizeChanged = { queueSize, leftSize ->
                        println("queue size changed to $queueSize")
                        launch(Dispatchers.Main) {
                            stateCollector.stateChanged(
                                newQueueSize = queueSize,
                                newBusyChannelsSize = null,
                                newQueueLeftSize = leftSize,
                                newFinishedRequests = null
                            )
                        }
                    })
                smo = SMO(
                    queueSMO,
                    this,
                    muServiceFlow!!,
                    nChannels!!,
                    epochStartTime,
                    reportSizeChanged = { busyChannels: Int, allFinishedListSize: Int ->
                        println("SMO busy size changed to $busyChannels")
                        launch(Dispatchers.Main) {
                            stateCollector.stateChanged(
                                newQueueSize = null,
                                newBusyChannelsSize = busyChannels,
                                newQueueLeftSize = null,
                                newFinishedRequests = allFinishedListSize
                            )
                        }
                    }
                )
                launch {
                    requestProducer = RequestProducer(lambdaInputFlow!!, epochStartTime)
                    requestProducer.requestsFlow().collect { request ->
                        println("collecting ${request.id}, deltaFromEpoch = ${request.deltaFromEpoch}, deltaFromLast = ${request.deltaFromLastRequest}")
                        queueSMO.addRequest(request)

                    }

                }
            }.setUpCancellation()


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
            StatisticsCalculator.calculate(this)
            // TODO: положить срезы на графики 
            // TODO: подсчет теоретических/практических характеристик 
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

    private fun Job.setUpCancellation() {
        val processingJob = this
        val listener = ActionListener {
            processingJob.cancel()
            stopButtonClicked()
        }
        stopButton.addActionListener(listener)
        stopButton.isEnabled = true
        // update the status and remove the listener after the loading job is completed
        launch {
            processingJob.join()
            stopButton.removeActionListener(listener)
            stopButton.isEnabled = false
        }
    }
}