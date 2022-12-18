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
    private var lastState: State = State(0, 0, 0L, epochTime)

    suspend fun stateChanged(newQueueSize: Int?, newBusyChannelsSize: Int?) {
        mutex.withLock {
            val currentTime = currentTimeMillis()
            lastState.stateTime = currentTime - lastState.stateTimeStamp
            stateList += lastState
            val newLastState = State(
                queueSize = newQueueSize ?: lastState.queueSize,
                busyChannels = newBusyChannelsSize ?: lastState.busyChannels,
                stateTime = 0L,
                stateTimeStamp = currentTime
            )
            lastState = newLastState
        }
    }

    fun getStateList(): List<State> = stateList.toList()

}