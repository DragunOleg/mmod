package ipt_two

import ipt_two.model.State
import jetbrains.datalore.base.math.ipow
import kr_two.factorial
import kotlin.math.pow

object StatisticsCalculator {
    fun calculate(getter: IprTwoInputGetter) {
        val n = getter.nChannels!!
        val M = getter.mQueue!!
        val lambda = getter.lambdaInputFlow!!
        val mu = getter.muServiceFlow!!
        val nu = getter.nuLeaving!!

        println("~~~~~~~~~~~~~~~~~~~~~ТЕОРЕТИЧЕСКИЕ~~~~~~~~~~~~~~~~~~~~~~~~~~~")
        val y = lambda / mu
        println("y нагрузка = $y")
        // Предельные вероятности состояний:
        var sump0 = 1.0
        for (i in 1..n) {
            sump0 += y.pow(i) / factorial(i)
        }

        for (i in 1..M) {
            sump0 += y.pow(n + i) / (n.ipow(i) * factorial(n))
        }

        //(формула 2.23)
        val p0 = sump0.pow(-1)

        val pList = mutableListOf(p0)
        //(формула 2.24)
        for (i in 1..n) {
            val pIndexed = (y.pow(i) / factorial(i)) * p0
            pList.add(pIndexed)
        }
        //(формула 2.25)
        for (i in 1..M) {
            val pIndexed = (y.pow(n + i) / (n.ipow(i) * factorial(n))) * p0
            pList.add(pIndexed)
        }

        pList.forEachIndexed { index, d ->
            println("p${index}= $d")
        }
        println("p sum= ${pList.sum()}")
        //2.26
        val potk = pList.last()
        println("P отказа = $potk")
        //2.27
        val poch = pList.subList(n, n + M - 1).fold(0.0) { sum, element ->
            sum + element
        }
        println("P образования очереди = $poch")
        //2.28
        val Q = 1 - potk //отношение среднего числа заявок, обслуживаемых СМО в единицу времени, к среднему числу поступивших за это же время заявок.
        println("Q относительная пропускная способность = $Q")
        //2.29
        val lambda_ = lambda * Q //среднее число заявок, которое сможет обслужить СМО в единицу времени
        println("λ` абсолютная пропускная способность = $lambda_")
        val kzan = lambda_ / mu
        println("kzan среднее число занятых каналов = $kzan")

        val l = (y.pow(n + 1) / (n * factorial(n))) *
                ((1 - ((y / n).pow(M)) * (M + 1 - (M / n) * y)) /
                        ((1 - y / n).pow(2))) *
                p0
        println("l среднее число заявок, находящихся в очереди = $l")
        val w = l / lambda
        println("w среднее время ожидания в очереди = $w")
        val m = l + kzan
        println("m среднее число заявок в СМО = $m")
        val u = m / lambda
        println("среднеe время пребывания заявки в СМО = $u")

        println("~~~~~~~~~~~~~~~~~~~~~ПРАКТИЧЕСКИЕ~~~~~~~~~~~~~~~~~~~~~~~~~~~")

        val allFinishedList = getter.getAllFinishedList()
        val _potk2 = getter.getLeftQueueSize().toDouble() / getter.getRequestProducedSize()
        var (invalidStates, validStates) = getter.stateCollector.getStateList().toMutableList()
            .partition { it.busyChannels < n && it.queueSize > 0 }
        val validStatesTime = validStates.sumOf { it.stateTime }.also { println("validStatesTime = $it") }
        val invalidStatesTime = invalidStates.sumOf { it.stateTime }.also { println("invalidStatesTime = $it") }
        validStates = validStates.toMutableList()
        //добавляем процессинговое инвалидное время к нулевому состоянию
        validStates.add(State(0, 0, invalidStatesTime, 0L))

        val _pList = mutableListOf<Double>()
        for (i in 0..n) {
            validStates
                .filter { it.queueSize == 0 && it.busyChannels == i }
                .fold(0L) { sum, element -> sum + element.stateTime }
                .apply { _pList.add(this.toDouble() / (validStatesTime + invalidStatesTime)) }
        }

        for (i in 1..M) {
            validStates
                .filter { it.queueSize == i && it.busyChannels == n }
                .fold(0L) { sum, element -> sum + element.stateTime }
                .apply { _pList.add(this.toDouble() / (validStatesTime + invalidStatesTime)) }
        }
        _pList.forEachIndexed { index, d ->
            println("_p${index}= $d")
        }
        println("_p sum= ${_pList.sum()}")

        val _potk1 = _pList.last()
        println("_p отказа = $_potk1")
        println("_p отказа (по ушедшим) = $_potk2")

        var _kzan = 0.0
        for (i in 0..n) {
            _kzan += i *
                    (validStates
                        .filter { it.busyChannels == i }
                        .sumOf { it.stateTime.toDouble() } /
                            (validStatesTime + invalidStatesTime))
        }
        println("_kzan среднее число занятых каналов = $_kzan")

        var _l = 0.0
        for (i in 0..M) {
            _l += i *
                    (validStates
                        .filter { it.queueSize == i }
                        .sumOf { it.stateTime.toDouble() } /
                            (validStatesTime + invalidStatesTime))
        }
        println("_l среднее число заявок, находящихся в очереди = $_l")

        val _m = _l + _kzan
        println("_m среднее число заявок в СМО = $_m")

        //ПРАКТИЧЕСКОE среднее число заявок, обслуженное в единицу времени
        val _lambda_ = allFinishedList.size / (validStatesTime + invalidStatesTime) * MILLIS_IN_SECOND
        println("_λ` абсолютная пропускная способность = $_lambda_")


        val _w = allFinishedList
            .map { it.queueWaitingTime }
            .average() / MILLIS_IN_SECOND
        println("_w среднее время ожидания в очереди = $_w")

        val _p = allFinishedList
            .map { it.serviceWaitingTime }
            .average() / MILLIS_IN_SECOND
        println("_p среднее время обслуживания в канале = $_p")


        val _u = _w + _p
        println("среднеe время пребывания заявки в СМО = $_u")
        // TODO: ГРАФИКИ 
    }
}