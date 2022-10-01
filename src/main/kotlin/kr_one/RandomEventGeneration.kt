package kr_one

fun main() {
    val randomEventGenerator = RandomEventGenerator(isRealRandom = true, isDebug = true)
    val Pa = 0.45
    val result = randomEventGenerator(Pa)
}

/**
 * @param isRealRandom: true if you want real random. False if you want repeatable pseudoRandom
 * @param isDebug: should println results or not
 */
class RandomEventGenerator(
    private val isRealRandom: Boolean,
    private val isDebug: Boolean
) {
    private val realRandom = java.util.Random()
    private val pseudoRandom = kotlin.random.Random(10)

    private fun realRandomNext() = realRandom.nextDouble()
    private fun pseudoRandomNext() = pseudoRandom.nextDouble()

    operator fun invoke(Pa: Double): Result {
        val randomNumberX = if (isRealRandom) realRandomNext() else pseudoRandomNext()
        val result = randomNumberX <= Pa

        if (isDebug) {
            println(
                "x = $randomNumberX \n" +
                        "Pa = $Pa"
            )
            if (result) {
                println("Event happened")
            } else {
                println("Event didn't happened")
            }
        }

        return Result(result = randomNumberX <= Pa, Pa = Pa, randomNumber = randomNumberX)
    }

    /**
     * @param result is true, if event happened
     */
    data class Result(
        val result: Boolean,
        val Pa: Double,
        val randomNumber: Double
    )
}

