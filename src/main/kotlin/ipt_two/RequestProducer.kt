package ipt_two

import ipt_two.model.Request
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

    fun getRequestProducedSize() = i + 1

    fun requestsFlow(): Flow<Request> = flow {
        while(true) {
            i++
            //через это кол-во миллисикунд мы добавляем заявку в поток
            var t = (distributionGenerator.sample() * MILLIS_IN_SECOND).toLong()
            //при очень высокой интенсивности не позволяем времени быть нулями, чтобы не было одновременно выпущенных заявок
            if (t == 0L) {
                t = 1
            }
            delay(t)
            val systemTime: Long = System.currentTimeMillis()
            println("emiting id $i")
            emit(Request(id = i, deltaFromLastRequest = t, deltaFromEpoch = systemTime - epochTime, issueTime = systemTime))
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

fun main() {
    RequestProducer(
        lambdaInputFlow = 100.0,
        epochTime = System.currentTimeMillis()
    ).testDistribution()
}