package ipr_two

import ipr_two.model.Request
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
    private val epochTime: Long,
    //число занятых каналов поменялось
    val reportSizeChanged: (busyChannels: Int, allFinishedListSize: Int) -> Unit
) {
    //список каналов обслуживания
    private val channels: List<ChannelSMO>
    //канал с завершенными заявками, все каналы обсуживания сбрасывают заявки сюда
    private val finishedRequests = Channel<Request>()
    private val allFinishedList = mutableListOf<Request>()
    fun getAllFinishedList(): List<Request> = allFinishedList.toList()

    init {
        channels = List(nChannels) { ChannelSMO(id = it, muServiceFlow, coroutineScope, epochTime, finishedRequests) }
        subscribeToFinished()
        coroutineScope.launch {
            while (true) {
                delay(1)
                channels.firstOrNull { it.isAvailable }?.also { channelsSMO ->
                    queueSMO.getRequest()?.let { request ->
                        channelsSMO.putRequest(request)
                        reportSizeChanged(channels.filter { !it.isAvailable }.size, allFinishedList.size)
                    }
                }
            }
        }
    }

    private fun subscribeToFinished() {
        coroutineScope.launch {
            finishedRequests.receiveAsFlow().collect { request ->
                println("request id ${request.id} finished service time: ${request.serviceWaitingTime}")
                allFinishedList += request
                reportSizeChanged(channels.filter { !it.isAvailable }.size, allFinishedList.size)
                println("finishedListSize = ${allFinishedList.size}")
            }
        }
    }
}