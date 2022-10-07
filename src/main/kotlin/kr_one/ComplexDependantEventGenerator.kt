package kr_one

fun main() {
    val complexDependantEventGenerator = ComplexDependantEventGenerator(isRealRandom = true, isDebug = false)
    var Pa = 0.1
    var Pb = 0.1
    var PBdependant_A = 0.1

//    repeat(10) { times1 ->
//        Pa = 0.1 * times1
//        Pb = 0.0
//        repeat(10) { times2 ->
//            Pb = 0.1 * times2
//            PBdependant_A = 0.0
//            repeat(10) { times3 ->
//                PBdependant_A = 0.1 * times3
//                complexDependantEventGenerator.invoke(Pa, Pb, PBdependant_A)
//            }
//        }
//    }

    val resultList = List<ComplexDependantEventGenerator.Result> (100000) {complexDependantEventGenerator.invoke(Pa, Pb, PBdependant_A)}
    val ABList = resultList.filterIsInstance<ComplexDependantEventGenerator.Result.AB>().apply {
        println("AB size: $size")
    }
    val notA_BList = resultList.filterIsInstance<ComplexDependantEventGenerator.Result.notA_B>().apply {
        println("notA_B size: $size")
    }
    val A_notBList = resultList.filterIsInstance<ComplexDependantEventGenerator.Result.A_notB>().apply {
        println("A_notB size: $size")
    }
    val notA_notBList = resultList.filterIsInstance<ComplexDependantEventGenerator.Result.notA_notB>().apply {
        println("notA_notB size: $size")
    }

}

// TODO: P(B/!A) верно или нет?
// TODO: Tests
class ComplexDependantEventGenerator(
    val isRealRandom: Boolean,
    val isDebug: Boolean
) {
    private val randomEventGenerator = RandomEventGenerator(isRealRandom = isRealRandom, isDebug = false)

    operator fun invoke(Pa: Double, Pb: Double, PBdependantA: Double): Result {
        val PBdependant_notA = calculate_PBdepentant_notA(Pa, Pb, PBdependantA)
        val resultA = randomEventGenerator(Pa)
        val resultB = randomEventGenerator(Pb)

        val complexDependantEventResult =
            if (resultA.result) { //x1<=Pa
                if (resultB.randomNumber <= PBdependantA) { //x2<=P(B/A)
                    Result.AB(Pa, Pb, PBdependantA, PBdependant_notA, resultA.randomNumber, resultB.randomNumber)
                } else { //x2>P(B/A)
                    Result.A_notB(Pa, Pb, PBdependantA, PBdependant_notA, resultA.randomNumber, resultB.randomNumber)
                }
            } else {  //x1>Pa
                if (resultB.randomNumber <= PBdependant_notA) { //x2<=P(B/!A)
                    Result.notA_B(Pa, Pb, PBdependantA, PBdependant_notA, resultA.randomNumber, resultB.randomNumber)
                } else { //x2>P(B/!A)
                    Result.notA_notB(Pa, Pb, PBdependantA, PBdependant_notA, resultA.randomNumber, resultB.randomNumber)
                }
            }
        if (isDebug) println(complexDependantEventResult.debugString())
        return complexDependantEventResult

    }

    /**
     * Возьмем за А - событие из RandomEventGenerator.
     * Нам не важно, произошло ли B в аналогичном понимании, будем брать его только для формулы
     * формула полной ветоятности:
     * P(B)=P(B/A)*P(A) +P(B/!A)*P(!A), отсюда
     *
     * P(B)-P(B/A)*P(A)
     * -------------- = P(B/!A)
     *      P(!A)
     *
     */
    private fun calculate_PBdepentant_notA(Pa: Double, Pb: Double, PBdependantA: Double): Double {
        //если невозможно событие !A, то и P(B/!A) невозможно (или = 0)
        //if (Pa == 1.0) return 0.0
        //если невозможно событие B, то и зависимые события невозможны
        //if (Pb == 0.0) return 0.0
        //вероятность не может быть больше 1.0 и меньше 0.0
        val PBdependant_notA = (Pb - PBdependantA * Pa) / (1.0 - Pa)

        return PBdependant_notA
    }

    /**
     * Тут B = именно зависимое событие. Не путать с x2 <= Pb
     */
    sealed class Result(
        private val Pa: Double,
        private val Pb: Double,
        private val PBdependant_A: Double,
        private val PBdependant_notA: Double,
        private val x1: Double,
        private val x2: Double
    ) {
        class AB(Pa: Double, Pb: Double, PBdependantA: Double, PBdependant_notA: Double, x1: Double, x2: Double) :
            Result(Pa, Pb, PBdependantA, PBdependant_notA, x1, x2)

        class A_notB(Pa: Double, Pb: Double, PBdependantA: Double, PBdependant_notA: Double, x1: Double, x2: Double) :
            Result(Pa, Pb, PBdependantA, PBdependant_notA, x1, x2)

        class notA_B(Pa: Double, Pb: Double, PBdependantA: Double, PBdependant_notA: Double, x1: Double, x2: Double) :
            Result(Pa, Pb, PBdependantA, PBdependant_notA, x1, x2)

        class notA_notB(
            Pa: Double,
            Pb: Double,
            PBdependantA: Double,
            PBdependant_notA: Double,
            x1: Double,
            x2: Double
        ) :
            Result(Pa, Pb, PBdependantA, PBdependant_notA, x1, x2)

        fun debugString(): String {
            return ("Pa = $Pa, Pb = $Pb, PBdependant_A = $PBdependant_A, PBdependant_notA = $PBdependant_notA"
                    //+ "\n ${this.javaClass.simpleName} : x1 = $x1, x2 = $x2"
                    )
        }
    }
}