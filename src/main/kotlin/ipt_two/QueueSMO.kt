package ipt_two

import ipt_two.model.Request
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.html.currentTimeMillis

class QueueSMO(
    val capacityM: Int,
    val epochTime: Long,
    val reportSizeChanged: (Int) -> Unit
) {
    private val mutex = Mutex()
    private val queueList = mutableListOf<Request>()
    private val leftList = mutableListOf<Request>()

    suspend fun getRequest(): Request? {
        var result: Request? = null
        modifyQueue {
            // TODO: проверка списка на уставшие заявки
            result = this.firstOrNull()?.also {
                it.queueWaitingTime = currentTimeMillis() - it.issueTime
                this.removeFirst()
                reportSizeChanged(this.size)
            }
        }
        return result
    }

    suspend fun addRequest(request: Request) {
        modifyQueue {
            // TODO: проверка списка на уставшие заявки
            if (this.size < capacityM) {
                this.add(request)
                reportSizeChanged(this.size)
            } else {
                request.isFaceFullQueue = true
                leftList.add(request)
                println("leftQueue = ${leftList.size}")
            }
        }
    }

    private suspend fun modifyQueue(block: MutableList<Request>.()-> Unit) {
        mutex.withLock {
            queueList.block()
        }
    }
}