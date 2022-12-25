package ipr_two

import ipr_two.model.Request
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.apache.commons.math3.distribution.ExponentialDistribution

class ChannelSMO(
    private val id: Int,
    muServiceFlow: Double,
    private val scope: CoroutineScope,
    val epochTime: Long,
    //сюда кладем заявку при выходе обслуженной
    val finishedRequests : Channel<Request>
) {
    private val distributionGenerator = ExponentialDistribution(1.0/muServiceFlow)
    var isAvailable: Boolean = true
    private var millisLoaded = 0L

    fun putRequest(request: Request) {
        isAvailable = false
        scope.launch {
            var t = (distributionGenerator.sample() * MILLIS_IN_SECOND).toLong()
            //при очень высокой интенсивности не позволяем времени быть нулями, чтобы не было одновременно выпущенных заявок
            if (t == 0L) {
                t = 1
            }
            delay(t)
            millisLoaded += t
            request.serviceWaitingTime = t
            isAvailable = true
            finishedRequests.send(request)
        }
    }
}