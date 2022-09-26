package kr_one

fun main() {
    val randomEventGenerator = RandomEventGenerator()
    val Pa = 0.45
    randomEventGenerator(Pa)
}

class RandomEventGenerator {
    private val realRandom = java.util.Random()
    private val pseudoRandom = kotlin.random.Random(10)

    fun realRandomNext() = realRandom.nextDouble()
    fun pseudoRandomNext() = pseudoRandom.nextDouble()

    /**
     * Return true, if event A triggered
     */
    operator fun invoke(Pa: Double): Boolean {
        val randomNumberX = realRandomNext()
        //val randomNumberX = pseudoRandomNext()
        val result = randomNumberX <= Pa
        println("x = $randomNumberX \n" +
                "Pa = $Pa")
        if (result) {
            println("Event A happened")
        } else {
            println("Event A didn't happened")
        }
        return randomNumberX <= Pa
    }
}

