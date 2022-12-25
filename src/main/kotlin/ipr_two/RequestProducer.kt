package ipr_two

import ipr_two.model.Request
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.apache.commons.math3.distribution.ExponentialDistribution
import org.jetbrains.letsPlot.geom.geomHistogram
import org.jetbrains.letsPlot.letsPlot

const val MILLIS_IN_SECOND = 1000

class RequestProducer(
    val lambdaInputFlow: Double,
    val nuLeaving: Double,
    val epochTime: Long
) {
    private val lambdaDistributionGenerator = ExponentialDistribution(1.0 / lambdaInputFlow)
    private val nuDistributionGenerator = ExponentialDistribution(1.0 / nuLeaving)
    private var i: Long = 1

    fun getRequestProducedSize() = i

    fun requestsFlow(): Flow<Request> = flow {
        while (true) {
            //через это кол-во миллисикунд мы добавляем заявку в поток
            var t = (lambdaDistributionGenerator.sample() * MILLIS_IN_SECOND).toLong()
            //сколько времени готова простоять в очереди
            var impatientQueueLeavingTime = (nuDistributionGenerator.sample() * MILLIS_IN_SECOND).toLong()
            //при очень высокой интенсивности не позволяем времени быть нулями, чтобы не было одновременно выпущенных заявок
            if (t == 0L) {
                t = 1
            }
            //даем процессору немного времени пройти сквозь пустую очередь
            if (impatientQueueLeavingTime <=4L) {
                impatientQueueLeavingTime = 5L
            }
            delay(t)
            val systemTime: Long = System.currentTimeMillis()
            println("emiting id $i, impatientTime = $impatientQueueLeavingTime")
            emit(
                Request(
                    id = i,
                    deltaFromLastRequest = t,
                    deltaFromEpoch = systemTime - epochTime,
                    issueTime = systemTime,
                    impatientQueueLeavingTime = impatientQueueLeavingTime
                )
            )
            i++
        }
    }


    fun testDistribution() {
        val data = mapOf<String, List<*>>(
            "x" to List(100000) { lambdaDistributionGenerator.sample() }
        )
        val p = letsPlot(data) { x = "x" }
        (p + geomHistogram(boundary = 0.0)).show()
    }
}

fun main() {
    RequestProducer(
        lambdaInputFlow = 1.0,
        nuLeaving = 0.0,
        epochTime = System.currentTimeMillis()
    ).testDistribution()
}