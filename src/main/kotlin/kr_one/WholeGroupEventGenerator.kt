package kr_one

import org.jetbrains.letsPlot.geom.geomHistogram
import org.jetbrains.letsPlot.ggsize
import org.jetbrains.letsPlot.letsPlot

fun main() {
    //remove annoying warning "Graphics2D from BufferedImage lacks BUFFERED_IMAGE hint", was actual for 1/2 PC
    System.setProperty("org.apache.batik.warn_destination", "false")

    val wholeGroupEventGenerator = WholeGroupEventGenerator(isReadRandom = true, isDebug = true)
    val group = listOf<Double>(
        0.0,
        (1.0 / 8),
        (2.0 / 8),
        (5.0 / 8)
    )
    wholeGroupEventGenerator(group, 0.1)
    wholeGroupEventGenerator(group, 0.35)
    wholeGroupEventGenerator(group, 0.799999)

    wholeGroupEventGenerator(group)
    drawExample()
}

class WholeGroupEventGenerator(
    isReadRandom: Boolean,
    private val isDebug: Boolean
) {
    companion object {
        private const val PA_CONSTANT = 1.0
    }

    private val randomEventGenerator = RandomEventGenerator(isRealRandom = isReadRandom, isDebug = false)

    /**
     * @return index of pGroupElement
     * pGroup[0] SHOULD be 0.0
     * PGroup element should be power of 2.
     * https://stackoverflow.com/questions/15625556/adding-and-subtracting-doubles-are-giving-strange-results
     */
    operator fun invoke(
        pGroup: List<Double>,
        predefinedX: Double? = null
    ): Result {
        validatePGroup(pGroup)?.apply { throw this }

        val resultX = predefinedX ?: randomEventGenerator(PA_CONSTANT).randomNumber
        var pSum = 0.0
        pGroup.forEachIndexed { index, d ->
            pSum += d
            if (resultX < pSum) {
                if (isDebug) println(
                    "resultX = $resultX \n" +
                            "pGroup = $pGroup \n" +
                            "index to return = $index \n" +
                            "pGroup element = $d \n"
                )
                return Result(
                    pGroup,
                    index,
                    resultX
                )
            }
        }
        //shouldn't be the case
        return Result(listOf(), 0, 0.0)
    }

    data class Result(
        val group: List<Double>,
        val indexBelonging: Int,
        val randomNumber: Double
    )

    /**
     * Validate input
     */
    private fun validatePGroup(list: List<Double>): Exception? {
        if (list.sum() != 1.0)
            return Exception("Group is not full, rebalance it to have 1.0 sum")
        if (list.size < 2)
            return Exception("List is empty, remake!")
        if (list[0] != 0.0)
            return Exception("P0 is not 0.0")
        return null

    }
}

private fun drawExample() {
    val wholeGroupEventGenerator = WholeGroupEventGenerator(isReadRandom = true, isDebug = false)
    val group = listOf<Double>(
        0.0,
        (1.0 / 16),
        (2.0 / 16),
        (5.0 / 16),
        (2.0 / 16),
        (2.0 / 16),
        (4.0 / 16)
    )
    val possibleEventSublist = group
        .mapIndexed { index, _ ->
            index to "A$index"
        }

    val list = List(10000) { wholeGroupEventGenerator.invoke(group) }

    val resultCondList: MutableList<String> = mutableListOf()
    val resultXList: MutableList<Double> = mutableListOf()

    possibleEventSublist.forEach { possibleEvent ->
        val resultXListIteration = list
            .filter { it.indexBelonging == possibleEvent.first }
            .map { it.randomNumber }
        resultCondList += list
            .filter {it.indexBelonging == possibleEvent.first}
            .map { possibleEvent.second }
        resultXList += resultXListIteration
        println("event ${possibleEvent.second}: size = ${resultXListIteration.size}")
    }


    val data = mapOf<String, Any>(
        "cond" to resultCondList,
        "x" to resultXList
    )
    val p = letsPlot(data) {x = "x"; fill = "cond"} + ggsize(800, 500)

    (p+geomHistogram(binWidth = 0.01)).show()
}
