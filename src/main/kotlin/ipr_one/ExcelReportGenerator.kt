package ipr_one

import kr_two.OSDetector
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.io.File
import java.io.FileOutputStream


class ExcelReportGenerator {
    val workbook = HSSFWorkbook()

    fun generateProbabilityMatrix(n: Int, m: Int, matrixString: String): Array<DoubleArray> {
        if (workbook.getSheetIndex(MATRIX_SHEET_NAME) != -1) {
            workbook.removeSheetAt(workbook.getSheetIndex(MATRIX_SHEET_NAME))
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
        repeat(m) { rowIndex ->
            sheet.createRow(rowIndex).apply {
                repeat(n) { columnIndex ->
                    val value = matrix[rowIndex][columnIndex]
                    summ += value
                    createCell(columnIndex).setCellValue(value)
                }
            }
        }
        //https://stackoverflow.com/questions/15625556/adding-and-subtracting-doubles-are-giving-strange-results
        if (summ != 1.0) throw Exception("Сумма вероятностей не равна 1. Вводите степени двойки, чтобы такого точно не было")
        repeat(n) {
            sheet.setColumnWidth(it, 13*256)
        }
        println("Сумма всех ячеек: $summ")
        FileOutputStream(PATH).use { fileOut ->
            workbook.write(fileOut)
            workbook.close()
            fileOut.close()
        }
        OSDetector.openWithSystem(File(PATH))
        return matrix
    }

    companion object {
        private const val PATH = "src/main/kotlin/ipr_one/CDCB_report.xls"
        private const val MATRIX_SHEET_NAME = "Матрица вероятностей"
    }
}