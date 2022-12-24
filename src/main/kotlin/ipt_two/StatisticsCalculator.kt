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
        println("λ` абсолютная пропускная способность = $lambda_") //todo ПРАКТИЧЕСКОE среднее число заявок, обслуженное в единицу времени
        val kzan = lambda_ / mu
        println("kzan среднее число занятых каналов = $kzan") //todo ПРАКТИЧЕСКОЕ  для каждого состояния из маппинга состояний

        val l = (y.pow(n + 1) / (n * factorial(n))) *
                ((1 - ((y / n).pow(M)) * (M + 1 - (M / n) * y)) /
                        ((1 - y / n).pow(2))) *
                p0
        println("l среднее число заявок, находящихся в очереди = $l") //todo ПРАКТИЧЕСКОЕ  для каждого состояния из маппинга состояний
        val w = l / lambda
        println("w среднее время ожидания в очереди = $w") //todo ПРАКТИЧЕСКОЕ
        val m = l + kzan
        println("m среднее число заявок в СМО = $m") //todo ПРАКТИЧЕСКОЕ  для каждого состояния из маппинга состояний
        val u = m / lambda
        println("средне время пребывания заявки в СМО = $u") //todo ПРАКТИЧЕСКОЕ

        println("~~~~~~~~~~~~~~~~~~~~~ПРАКТИЧЕСКИЕ~~~~~~~~~~~~~~~~~~~~~~~~~~~")

        val _potk2 = getter.getLeftQueueSize().toDouble() / getter.getRequestProducedSize()
        var (invalidState, validStates) = getter.stateCollector.getStateList().toMutableList()
            .partition { it.busyChannels< n && it.queueSize > 0 }
        val validStatesTime = validStates.sumOf { it.stateTime }.also { println("validStatesTime = $it") }
        val invalidStatesTime = invalidState.sumOf { it.stateTime }.also { println("invalidStatesTime = $it") }
        validStates = validStates.toMutableList()
        //добавляем процессинговое инвалидное время к нулевому состоянию
        validStates.add(State(0,0,invalidStatesTime,0L))

        val _pList = mutableListOf<Double>()
        for (i in 0..n) {
            validStates
                .filter { it.queueSize == 0 && it.busyChannels == i }
                .fold(0L) {sum, element -> sum + element.stateTime}
                .apply { _pList.add(this.toDouble() / (validStatesTime + invalidStatesTime)) }
        }

        for (i in 1..M) {
            validStates
                .filter { it.queueSize == i && it.busyChannels == n }
                .fold(0L) {sum, element -> sum + element.stateTime}
                .apply { _pList.add(this.toDouble() / (validStatesTime + invalidStatesTime)) }
        }
        _pList.forEachIndexed { index, d ->
            println("_p${index}= $d")
        }
        println("_p sum= ${_pList.sum()}")

        val _potk1 = _pList.last()
        println("_p отказа = $_potk1")
        println("_p отказа (по ушедшим) = $_potk2")

    }
}