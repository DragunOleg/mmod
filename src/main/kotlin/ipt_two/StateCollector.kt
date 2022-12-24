package ipt_two

import ipt_two.model.State
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.html.currentTimeMillis

class StateCollector(
    val epochTime: Long
) {
    private val mutex = Mutex()
    private val stateList = mutableListOf<State>()
    private var lastState: State = State(0, 0, 0L, epochTime, 0, 0)

    suspend fun stateChanged(newQueueSize: Int?, newBusyChannelsSize: Int?, newQueueLeftSize: Int?, newFinishedRequests: Int?) {
        mutex.withLock {
            val currentTime = currentTimeMillis()
            lastState.stateTime = currentTime - lastState.stateTimeStamp
            stateList += lastState
            val newLastState = State(
                queueSize = newQueueSize ?: lastState.queueSize,
                busyChannels = newBusyChannelsSize ?: lastState.busyChannels,
                stateTime = 0L,
                stateTimeStamp = currentTime,
                queueLeftSize = newQueueLeftSize ?: lastState.queueLeftSize,
                finishedRequests = newFinishedRequests ?: lastState.finishedRequests
            )
            lastState = newLastState
        }
    }

    fun getStateList(): List<State> = stateList.toList()

}