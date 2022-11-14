package ipr_one

import kr_two.OSDetector
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.io.File
import java.io.FileOutputStream


class ExcelReportGenerator {
    val workbook = HSSFWorkbook()

    fun generateMatrix(vectorA: List<Double>, vectorB: List<Double>):Array<DoubleArray> {
        val matrix = Array(vectorA.size) {row->
            DoubleArray(vectorB.size) {column ->
                vectorA[row] * vectorB[column]
            }
        }
        val sheet = workbook.createSheet(MATRIX_SHEET_NAME)
        repeat(vectorB.size) {rowIndex ->
            sheet.createRow(rowIndex).apply {
                repeat(vectorA.size) {columnIndex ->
                    createCell(columnIndex).setCellValue(matrix[columnIndex][rowIndex])
                }
            }

        }
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