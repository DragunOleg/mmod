package ipr_one

import kr_two.OSDetector
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import java.io.File
import java.io.FileOutputStream


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
        //for 2nd process
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
                    repeat(n) {columnIndex ->
                        createCell(columnIndex + 1).apply {
                            setCellValue(vectorA[columnIndex].toString())
                            setCellStyle(vectorACellStyle)
                        }
                    }
                }
            } else {
                sheet.createRow(rowIndex).apply {
                    repeat(n+1) { columnIndex ->
                        if (columnIndex == 0) {
                            createCell(0).apply {
                                setCellValue(vectorB[rowIndex-1].toString())
                                setCellStyle(vectorBCellStyle)
                            }
                        } else {
                            val value = matrix[rowIndex-1][columnIndex-1]
                            summ += value
                            createCell(columnIndex).setCellValue(value)
                        }
                    }
                }
            }
        }
        //https://stackoverflow.com/questions/15625556/adding-and-subtracting-doubles-are-giving-strange-results
        if (summ != 1.0) throw Exception("Сумма вероятностей не равна 1. Степень двойки в знаменателе поправит проблему")
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
        repeat(m+1) { rowIndex ->
            if (rowIndex == 0) {
                sheet.createRow(0).apply {
                    createCell(0).setCellValue("emp.B\\A")
                    repeat(n) {columnIndex ->
                        createCell(columnIndex + 1).apply {
                            setCellValue(vectorA[columnIndex].toString())
                            setCellStyle(vectorACellStyle)
                        }
                    }
                }
            } else {
                sheet.createRow(rowIndex).apply {
                    repeat(n+1) { columnIndex ->
                        if (columnIndex == 0) {
                            createCell(0).apply {
                                setCellValue(vectorB[rowIndex-1].toString())
                                setCellStyle(vectorBCellStyle)
                            }
                        } else {
                            val value = empiricalMatrix[rowIndex-1][columnIndex-1]
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
        writeWorkbookToFile()
        return empiricalMatrix
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
    }
}