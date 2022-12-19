package ipt_two

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

        // TODO: ПРОБЛЕМА В ПОТОКЕ ОБСЛУЖИВАНИЯ. ОН НЕ РАВЕН ТОМУ, ЧТО ТЫ ДУМАЛ 
        println("~~~~~~~~~~~~~~~~~~~~~ТЕОРЕТИЧЕСКИЕ~~~~~~~~~~~~~~~~~~~~~~~~~~~")
        // в среднем непрерывно занятый канал будет выдавать у = λ/μ обслуженных заявок в единицу времени
        val y = lambda / mu
        println("y = $y")
        // Предельные вероятности состояний:
        var sump0 = 1.0
        for (i in 1..n) {
            sump0 += y.pow(i) / factorial(i)
        }

        sump0 += (y.pow(n + 1) / n * factorial(n)) * ((1 - (y / n).pow(M)) / (1 - y / n))
        //(формула 2.23)
        val p0 = sump0.pow(-1)
        println("p0= $p0")

        val pList = mutableListOf(p0)
        //(формула 2.24)
        for (i in 1..n) {
            val pIndexed = (y.pow(i) / factorial(i)) * p0
            pList.add(i, pIndexed)
        }
        //(формула 2.25)
        for (i in 1..M) {
            val pIndexed = (y.pow(n + i) / (n.ipow(i) * factorial(n))) * p0
            pList.add(i, pIndexed)
        }

        pList.forEachIndexed { index, d ->
            println("p${index}= $d")
        }
        println("p sum= ${pList.sum()}")
        //2.26
        val potk = pList.last()
        println("P отказа = $potk")
        //2.27
        val poch = pList.subList(n, n+ M-1).fold(0.0){ sum, element ->
            sum + element
        }
        println("P образования очереди = $poch")
        //2.28
        val Q = 1 - potk
        println("Q относительная пропускная способность = $Q")
        //2.29
        val lambda_ = lambda * Q
        println("λ` абсолютная пропускная способность = $lambda_")
        val kzan = lambda_ / mu
        println("kzan среднее число занятых каналов = $kzan")



        val finalStates = getter.stateCollector?.getStateList()
    }
}