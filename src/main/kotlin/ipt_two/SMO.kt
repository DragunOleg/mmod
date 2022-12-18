package ipt_two

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch


class SMO(
    private val queueSMO: QueueSMO,
    private val coroutineScope: CoroutineScope,
    private val muServiceFlow: Double,
    nChannels: Int,
    private val epochTime: Long
) {
    //список каналов обслуживания
    private val channels: List<ChannelSMO>
    //канал с завершенными заявками, все каналы обсуживания сбрасывают заявки сюда
    private val finishedRequests = Channel<Request>()
    private val allFinishedList= mutableListOf<Request>()

    init {
        channels = List(nChannels) { ChannelSMO(muServiceFlow, coroutineScope, epochTime, finishedRequests) }
        subscribeToFinished()
        coroutineScope.launch {
            while (true) {
                delay(5)
                channels.firstOrNull { it.isAvailable }?.also {channelsSMO ->
                    queueSMO.getRequest()?.let { request ->
                        channelsSMO.putRequest(request)
                    }
                }
            }
        }
    }

    private fun subscribeToFinished() {
        coroutineScope.launch {
            finishedRequests.receiveAsFlow().collect{ request->
                println("request id ${request.id} finished service time: ${request.serviceWaitingTime}")
                allFinishedList += request
                println("finishedListSize = ${allFinishedList.size}")
            }
        }
    }
}