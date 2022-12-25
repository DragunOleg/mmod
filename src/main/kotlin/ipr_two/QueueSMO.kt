package ipr_two

import ipr_two.model.Request
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.html.currentTimeMillis

class QueueSMO(
    val capacityM: Int,
    val epochTime: Long,
    val reportSizeChanged: (queueSize: Int, leftSize: Int, impatientLeftSize: Int) -> Unit
) {
    private val mutex = Mutex()
    private val queueList = mutableListOf<Request>()
    private val leftList = mutableListOf<Request>()
    //нетерпливые заявки попадают сюда
    private val impatientLeftList = mutableListOf<Request>()

    fun leftSize() = leftList.size
    fun leftImpatientSize() = impatientLeftList.size

    suspend fun getRequest(): Request? {
        var result: Request? = null
        modifyQueue {
            //удаляем уставшие заявки
            val currentTime = currentTimeMillis()
            val (impatient, valid) = this.partition { currentTime > (it.issueTime + it.impatientQueueLeavingTime) }
            if (impatient.isNotEmpty()) {
                impatientLeftList += impatient
                this.clear()
                this.addAll(valid)
                reportSizeChanged(this.size, leftSize(), leftImpatientSize())
            }

            result = this.firstOrNull()?.also {
                it.queueWaitingTime = currentTimeMillis() - it.issueTime
                this.removeFirst()
                reportSizeChanged(this.size, leftSize(), leftImpatientSize())
            }
        }
        return result
    }

    suspend fun addRequest(request: Request) {
        modifyQueue {
            //удаляем уставшие заявки
            val currentTime = currentTimeMillis()
            val (impatient, valid) = this.partition { currentTime > (it.issueTime + it.impatientQueueLeavingTime) }
            if (impatient.isNotEmpty()) {
                impatientLeftList += impatient
                this.clear()
                this.addAll(valid)
                reportSizeChanged(this.size, leftSize(), leftImpatientSize())
            }

            if (this.size < capacityM) {
                this.add(request)
                reportSizeChanged(this.size, leftSize(), leftImpatientSize())
            } else {
                request.isFaceFullQueue = true
                leftList.add(request)
                reportSizeChanged(this.size, leftSize(), leftImpatientSize())
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