package kr_two

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.io.File
import java.io.FileOutputStream

class CriticalChiSquareExcelFileGenerator {
    /**
     * @param alpha = уровень значимости
     * @param degreesOfFreedom = число степеней свободы
     * @param chiSqCalculated = подсчитанное хи квадрат
     * @result запуск эксель файла с посчитанным/критическим значением
     */
    operator fun invoke(alpha: Double, degreesOfFreedom: Int, chiSqCalculated: Double) {
        val workbook = HSSFWorkbook()
        val sheet = workbook.createSheet(SHEET_NAME)
        sheet.setColumnWidth(0, 27*256)

        sheet.createRow(0).apply {
            createCell(0).setCellValue("Уровень значимости альфа =")
            createCell(1).setCellValue(alpha)
        }
        sheet.createRow(1).apply {
            createCell(0).setCellValue("Число степеней свободы k =")
            createCell(1).setCellValue(degreesOfFreedom.toString())
        }
        sheet.createRow(2).apply {
            createCell(0).setCellValue("Критическое хиКвадрат =")
            //CHISQ.INV.RT == ХИ2.ОБР.ПХ в русском экселе
            createCell(1).cellFormula = "_xlfn.CHISQ.INV.RT(B1,B2)"
        }
        sheet.createRow(3).apply {
            createCell(0).setCellValue("Подсчитанное хиКвадрат =")
            createCell(1).setCellValue(chiSqCalculated)
        }

        val path = String.format(PATH, System.currentTimeMillis().toString())
        FileOutputStream(path).use { fileOut ->
            workbook.write(fileOut)
            workbook.close()
            fileOut.close()
        }
        OSDetector.openWithSystem(File(path))
    }

    companion object {
        private const val PATH = "src/main/kotlin/kr_two/experiments/%s_JavaBooks.xls"
        private const val SHEET_NAME = "ChiSquareDistCalculator"
    }
}
