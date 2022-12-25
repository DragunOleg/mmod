package ipr_two

import jetbrains.datalore.base.math.ipow
import kr_two.OSDetector
import kr_two.factorial
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.util.CellRangeAddress
import org.jetbrains.letsPlot.GGBunch
import org.jetbrains.letsPlot.geom.geomLine
import org.jetbrains.letsPlot.label.ggtitle
import org.jetbrains.letsPlot.letsPlot
import java.io.File
import java.io.FileOutputStream
import kotlin.math.pow

object StatisticsCalculator {
    fun calculate(getter: IprTwoInputGetter) {
        val n = getter.nChannels!!
        val M = getter.mQueue!!
        val lambda = getter.lambdaInputFlow!!
        val mu = getter.muServiceFlow!!
        val nu = getter.nuLeaving!!

        // TODO: Переписать статистику с учетом ню 
        println("~~~~~~~~~~~~~~~~~~~~~ТЕОРЕТИЧЕСКИЕ~~~~~~~~~~~~~~~~~~~~~~~~~~~")
        val myY = lambda / mu
        println("y нагрузка = $myY")
        // Предельные вероятности состояний:
        var sump0 = 1.0
        for (i in 1..n) {
            sump0 += myY.pow(i) / factorial(i)
        }

        for (i in 1..M) {
            sump0 += myY.pow(n + i) / (n.ipow(i) * factorial(n))
        }

        //(формула 2.23)
        val p0 = sump0.pow(-1)

        val pList = mutableListOf(p0)
        //(формула 2.24)
        for (i in 1..n) {
            val pIndexed = (myY.pow(i) / factorial(i)) * p0
            pList.add(pIndexed)
        }
        //(формула 2.25)
        for (i in 1..M) {
            val pIndexed = (myY.pow(n + i) / (n.ipow(i) * factorial(n))) * p0
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
        val Q =
            1 - potk //отношение среднего числа заявок, обслуживаемых СМО в единицу времени, к среднему числу поступивших за это же время заявок.
        println("Q относительная пропускная способность = $Q")
        //2.29
        val lambda_ = lambda * Q //среднее число заявок, которое сможет обслужить СМО в единицу времени
        println("λ` абсолютная пропускная способность = $lambda_")
        val kzan = lambda_ / mu
        println("kzan среднее число занятых каналов = $kzan")

        val l = (myY.pow(n + 1) / (n * factorial(n))) *
                ((1 - ((myY / n).pow(M)) * (M + 1 - (M / n) * myY)) /
                        ((1 - myY / n).pow(2))) *
                p0
        println("l среднее число заявок, находящихся в очереди = $l")
        val w = l / lambda
        println("w среднее время ожидания в очереди = $w")
        val m = l + kzan
        println("m среднее число заявок в СМО = $m")
        val u = m / lambda
        println("u среднеe время пребывания заявки в СМО = $u")

        println("~~~~~~~~~~~~~~~~~~~~~ПРАКТИЧЕСКИЕ~~~~~~~~~~~~~~~~~~~~~~~~~~~")

        val epochStartTime = getter.epochStartTime
        val allFinishedList = getter.getAllFinishedList()
        val _potk2 = getter.getLeftQueueSize().toDouble() / getter.getRequestProducedSize()
        val (invalidStates, validStates) = getter.stateCollector.getStateList().toMutableList()
            .partition { it.busyChannels < n && it.queueSize > 0 }
        val validStatesTime = validStates.sumOf { it.stateTime }.also { println("validStatesTime = $it") }
        val invalidStatesTime = invalidStates.sumOf { it.stateTime }.also { println("invalidStatesTime = $it") }

        val _pList = mutableListOf<Double>()
        for (i in 0..n) {
            validStates
                .filter { it.queueSize == 0 && it.busyChannels == i }
                .fold(0L) { sum, element -> sum + element.stateTime }
                .apply { _pList.add(this.toDouble() / validStatesTime) }
        }

        for (i in 1..M) {
            validStates
                .filter { it.queueSize == i && it.busyChannels == n }
                .fold(0L) { sum, element -> sum + element.stateTime }
                .apply { _pList.add(this.toDouble() / validStatesTime) }
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
                        .sumOf { it.stateTime.toDouble() } / validStatesTime)
        }
        println("_kzan среднее число занятых каналов = $_kzan")

        var _l = 0.0
        for (i in 0..M) {
            _l += i *
                    (validStates
                        .filter { it.queueSize == i }
                        .sumOf { it.stateTime.toDouble() } / validStatesTime)
        }
        println("_l среднее число заявок, находящихся в очереди = $_l")

        val _m = _l + _kzan
        println("_m среднее число заявок в СМО = $_m")

        //ПРАКТИЧЕСКОE среднее число заявок, обслуженное в единицу времени
        val _lambda_ = allFinishedList.size.toDouble() / validStatesTime * MILLIS_IN_SECOND
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

        /**
         * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Графики~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
         */

        val dataQueue = mapOf<String, List<*>>(
            "x" to validStates.map { (it.stateTimeStamp - epochStartTime).toDouble() / MILLIS_IN_SECOND },
            "y" to validStates.map { it.queueSize }
        )
        val pDataQueue = letsPlot(dataQueue, mapping = { x = "x"; y = "y" })
        val plotDataQueue = (pDataQueue +
                geomLine { y = "y" } +
                ggtitle("размер очереди"))

        val dataChannels = mapOf<String, List<*>>(
            "x" to validStates.map { (it.stateTimeStamp - epochStartTime).toDouble() / MILLIS_IN_SECOND },
            "y" to validStates.map { it.busyChannels }
        )
        val pDataChannels = letsPlot(dataChannels, mapping = { x = "x"; y = "y" })
        val plotDataChannels = (pDataChannels +
                geomLine { y = "y" } +
                ggtitle("занятые каналы"))

        val dataLeftRequests = mapOf<String, List<*>>(
            "x" to validStates.map { (it.stateTimeStamp - epochStartTime).toDouble() / MILLIS_IN_SECOND },
            "y" to validStates.map { it.queueLeftSize }
        )
        val pDataLeftRequests = letsPlot(dataLeftRequests, mapping = { x = "x"; y = "y" })
        val plotDataLeftRequests = (pDataLeftRequests +
                geomLine { y = "y" } +
                ggtitle("Покинули очередь, упершись в хвост"))

        val dataFinishedRequests = mapOf<String, List<*>>(
            "x" to validStates.map { (it.stateTimeStamp - epochStartTime).toDouble() / MILLIS_IN_SECOND },
            "y" to validStates.map { it.finishedRequests }
        )
        val pDataFinishedRequests = letsPlot(dataFinishedRequests, mapping = { x = "x"; y = "y" })
        val plotDataFinishedRequests = (pDataFinishedRequests +
                geomLine { y = "y" } +
                ggtitle("Покинули систему обслуженными"))

        val dataImpationedFinishedRequests = mapOf<String, List<*>>(
            "x" to validStates.map { (it.stateTimeStamp - epochStartTime).toDouble() / MILLIS_IN_SECOND },
            "y" to validStates.map { it.impatientLeftSize }
        )
        val pDataImpationedFinishedRequests = letsPlot(dataImpationedFinishedRequests, mapping = { x = "x"; y = "y" })
        val plotDataImpationedFinishedRequests = (pDataImpationedFinishedRequests +
                geomLine { y = "y" } +
                ggtitle("Покинули очередь, не дождавшись"))

        GGBunch()
            .addPlot(
                plotDataQueue, 0, 0, 500, 200
            )
            .addPlot(
                plotDataLeftRequests, 0, 250, 500, 200
            )
            .addPlot(
                plotDataChannels, 550, 0, 500, 200
            )
            .addPlot(
                plotDataFinishedRequests, 550, 250, 500, 200
            )
            .addPlot(
                plotDataImpationedFinishedRequests, 0, 500, 500, 200
            )
            .show()

        /**
         * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Excel~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
         */
        val workbook = HSSFWorkbook()
        val sheet = workbook.createSheet(SHEET_NAME).apply {
            setColumnWidth(0, 46 * 256)
            setColumnWidth(1, 16 * 256)
            setColumnWidth(2, 16 * 256)

        }
        val headerCellStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.LEMON_CHIFFON.getIndex()
            fillPattern = FillPatternType.SOLID_FOREGROUND
        }

        sheet.createRow(0).apply {
            createCell(0).setCellValue("n каналов =")
            createCell(1).setCellValue(n.toDouble())
        }
        sheet.createRow(1).apply {
            createCell(0).setCellValue("M мест в очереди =")
            createCell(1).setCellValue(M.toDouble())
        }
        sheet.createRow(2).apply {
            createCell(0).setCellValue("λ интенсивность входящего потока =")
            createCell(1).setCellValue(lambda)
        }
        sheet.createRow(3).apply {
            createCell(0).setCellValue("μ интенсивность потока обслуживания =")
            createCell(1).setCellValue(mu)
        }
        sheet.createRow(4).apply {
            createCell(0).setCellValue("ν параметр закона ухода(игнорируется в v1) =")
            createCell(1).setCellValue(nu)
        }

        sheet.createRow(5).apply {
            createCell(0).apply {
                setCellValue("Параметры:")
                setCellStyle(headerCellStyle)
            }
            createCell(1).apply {
                setCellValue("Теоретическое:")
                setCellStyle(headerCellStyle)
            }
            createCell(2).apply {
                setCellValue("Практическое:")
                setCellStyle(headerCellStyle)
            }
        }

        sheet.createRow(6).apply {
            createCell(0).setCellValue("P отказа =")
            createCell(1).setCellValue(potk)
            createCell(2).setCellValue(_potk1)
            createCell(3).setCellValue(_potk2)
        }
        sheet.createRow(7).apply {
            createCell(0).setCellValue("l среднее число заявок, находящихся в очереди =")
            createCell(1).setCellValue(l)
            createCell(2).setCellValue(_l)
        }
        sheet.createRow(8).apply {
            createCell(0).setCellValue("kzan среднее число занятых каналов =")
            createCell(1).setCellValue(kzan)
            createCell(2).setCellValue(_kzan)
        }
        sheet.createRow(9).apply {
            createCell(0).setCellValue("m среднее число заявок в СМО =")
            createCell(1).setCellValue(m)
            createCell(2).setCellValue(_m)
        }
        sheet.createRow(10).apply {
            createCell(0).setCellValue("w среднее время ожидания в очереди =")
            createCell(1).setCellValue(w)
            createCell(2).setCellValue(_w)
        }
        sheet.createRow(11).apply {
            createCell(0).setCellValue("u среднеe время пребывания заявки в СМО =")
            createCell(1).setCellValue(u)
            createCell(2).setCellValue(_u)
        }
        sheet.createRow(12).apply {
            createCell(0).setCellValue("λ` абсолютная пропускная способность =")
            createCell(1).setCellValue(lambda_)
            createCell(2).setCellValue(_lambda_)
        }
        sheet.createRow(13).apply {
            sheet.addMergedRegion(CellRangeAddress(13, 13, 0, 2))
            createCell(0).apply {
                setCellValue("финальные вероятности состояний")
                setCellStyle(headerCellStyle)
            }
        }

        pList.forEachIndexed { index, d ->
            sheet.createRow(14 + index).apply {
                createCell(0).setCellValue("p$index =")
                createCell(1).setCellValue(d)
                createCell(2).setCellValue(_pList[index])
            }
        }

        val path = String.format(PATH, System.currentTimeMillis().toString())
        FileOutputStream(path).use { fileOut ->
            workbook.write(fileOut)
            workbook.close()
            fileOut.close()
        }
        OSDetector.openWithSystem(File(path))
    }


    private const val PATH = "src/main/kotlin/ipr_two/reports/%s_JavaBooks.xls"
    private const val SHEET_NAME = "SMO report"

}