package kr_one

fun main() {
    val complexEventGenerator = ComplexEventGenerator(isRealRandom = true, isDebug = true)
    val Pa = 0.45
    val Pb = 0.9
    complexEventGenerator.invoke(Pa, Pb)
}

class ComplexEventGenerator(
    val isRealRandom: Boolean,
    val isDebug: Boolean
) {
    private val randomEventGenerator = RandomEventGenerator(isRealRandom = isRealRandom, isDebug = false)

    operator fun invoke(Pa: Double, Pb: Double): Result {
        val resultA = randomEventGenerator.invoke(Pa)
        val resultB = randomEventGenerator.invoke(Pb)

        val complexEventResult =
            if (resultA.result) {
                if (resultB.result) {
                    Result.AB(Pa = Pa, x1 = resultA.randomNumber, Pb = Pb, x2 = resultB.randomNumber)
                } else {
                    Result.A_notB(Pa = Pa, x1 = resultA.randomNumber, Pb = Pb, x2 = resultB.randomNumber)
                }
            } else {
                if (resultB.result) {
                    Result.notA_B(Pa = Pa, x1 = resultA.randomNumber, Pb = Pb, x2 = resultB.randomNumber)
                } else {
                    Result.notA_notB(Pa = Pa, x1 = resultA.randomNumber, Pb = Pb, x2 = resultB.randomNumber)
                }
            }
        if (isDebug) println(complexEventResult.debugString())
        return complexEventResult
    }

    sealed class Result(
        private val Pa: Double,
        private val x1: Double,
        private val Pb: Double,
        private val x2: Double
    ) {
        class AB(Pa: Double, x1: Double, Pb: Double, x2: Double) : Result(Pa, x1, Pb, x2)
        class A_notB(Pa: Double, x1: Double, Pb: Double, x2: Double) : Result(Pa, x1, Pb, x2)
        class notA_B(Pa: Double, x1: Double, Pb: Double, x2: Double) : Result(Pa, x1, Pb, x2)
        class notA_notB(Pa: Double, x1: Double, Pb: Double, x2: Double) : Result(Pa, x1, Pb, x2)

        fun debugString(): String {
            return ("${this.javaClass.simpleName} :" +
                    "Pa = $Pa, x1 = $x1, Pb = $Pb, x2 = $x2")
        }
    }
}