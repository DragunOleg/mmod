package ipr_one

import kr_two.OSDetector
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import java.io.File
import java.io.FileOutputStream
import kotlin.math.pow
import kotlin.math.sqrt


class ExcelReportGenerator {
    private val workbook = HSSFWorkbook()
    private val vectorACellStyle = workbook.createCellStyle().apply {
        fillForegroundColor = IndexedColors.LEMON_CHIFFON.getIndex()
        fillPattern = FillPatternType.SOLID_FOREGROUND
    }
    private val vectorBCellStyle = workbook.createCellStyle().apply {
        fillForegroundColor = IndexedColors.LIGHT_ORANGE.getIndex()
        fillPattern = FillPatternType.SOLID_FOREGROUND
    }

    fun generateProbabilityMatrix(
        n: Int,
        m: Int,
        matrixString: String,
        vectorA: List<Int>,
        vectorB: List<Int>
    ): Array<DoubleArray> {
        //for 2nd process button click
        repeat(workbook.numberOfSheets) {
            workbook.removeSheetAt(0)
        }
        val matrixWeights = matrixString.trim().split(" ").map { it.toInt() }
        val matrixSum = matrixWeights.sum().also { println("Сумма весов матрицы: $it") }
        val matrix = Array(m) { row ->
            DoubleArray(n) { column ->
                matrixWeights[row * n + column] / matrixSum.toDouble()
            }
        }
        val sheet = workbook.createSheet(MATRIX_SHEET_NAME)
        var summ = 0.0
        //shift n m to +1 to set vector values to table also
        repeat(m + 1) { rowIndex ->
            if (rowIndex == 0) {
                sheet.createRow(0).apply {
                    createCell(0).setCellValue("B\\A")
                    repeat(n) { columnIndex ->
                        createCell(columnIndex + 1).apply {
                            setCellValue(vectorA[columnIndex].toString())
                            setCellStyle(vectorACellStyle)
                        }
                    }
                }
            } else {
                sheet.createRow(rowIndex).apply {
                    repeat(n + 1) { columnIndex ->
                        if (columnIndex == 0) {
                            createCell(0).apply {
                                setCellValue(vectorB[rowIndex - 1].toString())
                                setCellStyle(vectorBCellStyle)
                            }
                        } else {
                            val value = matrix[rowIndex - 1][columnIndex - 1]
                            summ += value
                            createCell(columnIndex).setCellValue(value)
                        }
                    }
                }
            }
        }
        //https://stackoverflow.com/questions/15625556/adding-and-subtracting-doubles-are-giving-strange-results
        //if (summ != 1.0) throw Exception("Сумма вероятностей не равна 1. Степень двойки в знаменателе поправит проблему")
        repeat(n) {
            sheet.setColumnWidth(it, 13 * 256)
        }
        println("Сумма всех ячеек: $summ")
        return matrix
    }

    fun generateEmpiricalDistributionMatrix(
        n: Int,
        m: Int,
        list: List<Random2DValueGenerator.Result>,
        vectorA: List<Int>,
        vectorB: List<Int>
    ): Array<DoubleArray> {
        val empiricalMatrix = Array(m) { row ->
            DoubleArray(n) { column ->
                list.filter { it.row == row && it.column == column }.size / list.size.toDouble()
            }
        }
        val sheet = workbook.createSheet(EMPIRICAL_MATRIX_SHEET_NAME)
        var summ = 0.0
        //shift n m to +1 to set vector values to table also
        repeat(m + 1) { rowIndex ->
            if (rowIndex == 0) {
                sheet.createRow(0).apply {
                    createCell(0).setCellValue("emp.B\\A")
                    repeat(n) { columnIndex ->
                        createCell(columnIndex + 1).apply {
                            setCellValue(vectorA[columnIndex].toString())
                            setCellStyle(vectorACellStyle)
                        }
                    }
                }
            } else {
                sheet.createRow(rowIndex).apply {
                    repeat(n + 1) { columnIndex ->
                        if (columnIndex == 0) {
                            createCell(0).apply {
                                setCellValue(vectorB[rowIndex - 1].toString())
                                setCellStyle(vectorBCellStyle)
                            }
                        } else {
                            val value = empiricalMatrix[rowIndex - 1][columnIndex - 1]
                            summ += value
                            createCell(columnIndex).setCellValue(value)
                        }
                    }
                }
            }
        }
        repeat(n) {
            sheet.setColumnWidth(it, 13 * 256)
        }
        println("Сумма всех ячеек эмпирически: $summ")
        return empiricalMatrix
    }

    fun generateEstimatesSheet(
        list: List<Random2DValueGenerator.Result>,
        RVN: Int,
        theoreticalMatrix: Array<DoubleArray>,
        vectorA: List<Int>,
        vectorB: List<Int>
    ) {
        val reportList = mutableListOf<Pair<String, String>>()

        /**
         *________________________Анализируем a_______________________
         */
        val aList = list.map { it.a }
        val dA = aList.average().also {
            reportList.add("(A)выборочное среднее:" to it.toString())
        }

        val aS02 = 1.0 / (RVN - 1) * aList.fold(0.0) { sum, element ->
            sum + (element - dA).pow(2)
        }
        reportList.add("(A)Несмещенная состоятельная оценка дисперсии S02:" to aS02.toString())
        val aS2 = 1.0 / RVN * aList.fold(0.0) { sum, element ->
            sum + (element - dA).pow(2)
        }
        reportList.add("(A)Cмещенная состоятельная оценка дисперсии S2:" to aS2.toString())
        val aS0 = sqrt(aS02)
        reportList.add("(A)Состоятельная оценка среднеквадратичного отклонения:" to aS0.toString())
        repeat(5) { k ->
            val aak = 1.0 / RVN * aList.fold(0.0) { sum, element ->
                sum + element.toDouble().pow(k + 1)
            }
            reportList.add("(A)Выборочный начальный момент ${k + 1}-го порядка:" to aak.toString())
            val amuk = 1.0 / RVN + aList.fold(0.0) { sum, element ->
                sum + (element - dA).pow(k + 1)
            }
            reportList.add("(A)Выборочный центральный момент ${k + 1}-го порядка:" to amuk.toString())
        }
        /**
         *________________________Анализируем b_______________________
         */
        val bList = list.map { it.b }
        val dB = bList.average()
        reportList.add("(B)выборочное среднее" to dB.toString())
        val bS02 = 1.0 / (RVN - 1) * bList.fold(0.0) { sum, element ->
            sum + (element - dB).pow(2)
        }
        reportList.add("(B)Несмещенная состоятельная оценка дисперсии S02:" to bS02.toString())
        val bS2 = 1.0 / RVN * bList.fold(0.0) { sum, element ->
            sum + (element - dB).pow(2)
        }
        reportList.add("(B)Cмещенная состоятельная оценка дисперсии S2:" to bS2.toString())
        val bS0 = sqrt(bS02)
        reportList.add("(B)Состоятельная оценка среднеквадратичного отклонения:" to bS0.toString())
        repeat(5) { k ->
            val bak = 1.0 / RVN * bList.fold(0.0) { sum, element ->
                sum + element.toDouble().pow(k + 1)
            }
            reportList.add("(B)Выборочный начальный момент ${k + 1}-го порядка:" to bak.toString())
            val bmuk = 1.0 / RVN + bList.fold(0.0) { sum, element ->
                sum + (element - dB).pow(k + 1)
            }
            reportList.add("(B)Выборочный центральный момент ${k + 1}-го порядка:" to bmuk.toString())
        }
        /**
         *________________________Посчитаем ma и mb из теоретической таблицы_______________________
         */
        val mb = vectorB.foldIndexed(0.0) { index, sum, element ->
            sum + (theoreticalMatrix[index].sum() * element)
        }
        val ma = vectorA.foldIndexed(0.0) { index, sum, element ->
            sum + (theoreticalMatrix.map { it[index] }.sum() * element)
        }



        /**
         *________________________Анализируем a b_______________________
         */
        val kab = 1.0 / (RVN - 1) * list.fold(0.0) { sum, element ->
            sum + (element.a - dA) * (element.b - dB)
        }
        reportList.add("Несмещенная состоятельная оценка корреляционного момента kab:" to kab.toString())
        val rab = list.fold(0.0) { sum, element ->
            sum + (element.a - dA) * (element.b - dB)
        } / sqrt(
            aList.fold(0.0) { sum, element -> sum + (element - dA).pow(2) } *
                    bList.fold(0.0) { sum, element -> sum + (element - dB).pow(2) }
        )
        reportList.add("Состоятельная оценка коэффициента корреляции rab" to rab.toString())

        reportEstimates(reportList,ma, mb, RVN)
    }

    private fun reportEstimates(
        list: List<Pair<String, String>>,
        ma: Double,
        mb: Double,
        RVN: Int
    ) {
        val sheet = workbook.createSheet(ESTIMATES_SHEET_NAME)


        list.forEachIndexed { index, pair ->
            println(pair.first + " " + pair.second)
            sheet.createRow(index).apply {
                createCell(0).setCellValue(pair.first)
                pair.second.toDoubleOrNull()?.let {
                    createCell(1).setCellValue(it)
                } ?: createCell(1).setCellValue(pair.second)
            }
        }
        sheet.setColumnWidth(0, 60 * 256)
        sheet.setColumnWidth(1, 13 * 256)
        sheet.setColumnWidth(3, 21 * 256)
        sheet.setColumnWidth(4, 13 * 256)
        sheet.setColumnWidth(5, 27 * 256)
        sheet.setColumnWidth(6, 13 * 256)

        sheet.getRow(0).apply {
            createCell(3).setCellValue("RVN-1")
            createCell(4).setCellValue(RVN - 1.0)
        }
        sheet.getRow(1).apply {
            createCell(3).setCellValue("1-уровень значимости")
            createCell(4).setCellValue(0.01)
        }
        sheet.getRow(2).apply {
            createCell(3).setCellValue("t")
            //СТЬЮДЕНТ.ОБР.2Х
            createCell(4).cellFormula = "_xlfn.T.INV.2T(E2,E1)"
        }
        sheet.getRow(3).apply {
            createCell(5).setCellValue("Доверительный интервал ma")
        }
        sheet.getRow(4).apply {
            createCell(4).cellFormula = "B1-(B2*E3)/SQRT(E1)"
            createCell(5).setCellValue("<=ma<=")
            createCell(6).cellFormula = "B1+(B2*E3)/SQRT(E1)"

        }

        sheet.getRow(5).apply {
            createCell(5).setCellValue("Доверительный интервал mb")
        }
        sheet.getRow(6).apply {
            createCell(4).cellFormula = "B15-(B16*E3)/SQRT(E1)"
            createCell(5).setCellValue("<=ma<=")
            createCell(6).cellFormula = "B15+(B16*E3)/SQRT(E1)"
        }
        sheet.getRow(8).apply {
            createCell(5).setCellValue("Теоретическое ma:")
            createCell(6).setCellValue(ma)
        }
        sheet.getRow(9).apply {
            createCell(5).setCellValue("Теоретическое mb:")
            createCell(6).setCellValue(mb)
        }
    }

    fun generateDataListSheet(list: List<Random2DValueGenerator.Result>) {
        val sheet = workbook.createSheet(DATA_SHEET_NAME)
        sheet.createRow(0).apply {
            createCell(0).setCellValue("a")
            createCell(1).setCellValue("b")
            createCell(2).setCellValue("randomNumber")
        }
        list.forEachIndexed { index, result ->
            sheet.createRow(index + 1).apply {
                createCell(0).setCellValue(result.a.toDouble())
                createCell(1).setCellValue(result.b.toDouble())
                createCell(2).setCellValue(result.randomNumber)
            }
        }
        sheet.setColumnWidth(0, 13 * 256)
        sheet.setColumnWidth(1, 13 * 256)
        sheet.setColumnWidth(2, 13 * 256)
        writeWorkbookToFile()
    }

    private fun writeWorkbookToFile() {
        FileOutputStream(PATH).use { fileOut ->
            workbook.write(fileOut)
            workbook.close()
            fileOut.close()
        }
        OSDetector.openWithSystem(File(PATH))
    }

    companion object {
        private const val PATH = "src/main/kotlin/ipr_one/CDCB_report.xls"
        private const val MATRIX_SHEET_NAME = "Матрица вероятностей"
        private const val EMPIRICAL_MATRIX_SHEET_NAME = "Эмпирическая матрица вероятностей"
        private const val ESTIMATES_SHEET_NAME = "Оценки"
        private const val DATA_SHEET_NAME = "Данные"
    }
}