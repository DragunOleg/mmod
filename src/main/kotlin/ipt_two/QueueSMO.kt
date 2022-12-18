package ipt_two

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.html.currentTimeMillis

class QueueSMO(
    val capacityM: Int,
    val epochTime: Long
) {
    private val mutex = Mutex()
    private val queueList = mutableListOf<Request>()
    private val leftList = mutableListOf<Request>()

    suspend fun getRequest(): Request? {
        var result: Request? = null
        modifyQueue {
            // TODO: проверка списка на уставшие заявки
            result = this.firstOrNull()?.let {
                it.queueWaitingTime = currentTimeMillis() - it.issueTime
                this.removeFirst()
            }
        }
        return result
    }

    suspend fun addRequest(request: Request) {
        modifyQueue {
            // TODO: проверка списка на уставшие заявки
            if (this.size < capacityM) {
                this.add(request)
            } else {
                leftList.add(request)
            }
        }
    }

    private suspend fun modifyQueue(block: MutableList<Request>.()-> Unit) {
        mutex.withLock {
            queueList.block()
            currentTimeMillis()
            println("queueSize: ${queueList.size} leftSize: ${leftList.size} fromEpoch = ${currentTimeMillis()-epochTime}")
        }
    }
}