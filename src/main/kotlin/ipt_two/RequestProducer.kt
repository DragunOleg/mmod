package ipt_two

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.apache.commons.math3.distribution.ExponentialDistribution
import org.jetbrains.letsPlot.geom.geomHistogram
import org.jetbrains.letsPlot.letsPlot

const val MILLIS_IN_SECOND = 1000

class RequestProducer(
    val lambdaInputFlow: Double,
    val epochTime: Long
) {
    private val distributionGenerator = ExponentialDistribution(1.0/lambdaInputFlow)
    private var i: Long = 0

    fun requestsFlow(): Flow<Request> = flow {
        while(true) {
            //через это кол-во миллисикунд мы добавляем заявку в поток
            var t = (distributionGenerator.sample() * MILLIS_IN_SECOND).toLong()
            if (t == 0L) {
                t = 1
            }
            delay(t)
            val systemTime: Long = System.currentTimeMillis()
            println("emiting $i")
            emit(Request(i = i, deltaFromLastRequest = t, deltaFromEpoch = systemTime - epochTime, issueTime = systemTime))
            i++
        }
    }


    fun testDistribution() {
        val data = mapOf<String, List<*>>(
            "x" to List(100000) { distributionGenerator.sample() }
        )
        val p = letsPlot(data) { x="x" }
        (p+geomHistogram(boundary = 0.0)).show()
    }
}

//todo сделать генерацию критического времени ухода, по истечению которого заявка уходит из очереди
//Это можно сделать тут, добавив к issueTime критическую дельту
data class Request(
    val i: Long,
    val deltaFromLastRequest: Long,
    val deltaFromEpoch: Long,
    val issueTime: Long,
    var queueWaitingTime: Long = 0L
)

fun main() {
    RequestProducer(
        lambdaInputFlow = 1.0,
        epochTime = System.currentTimeMillis()
    ).testDistribution()
}