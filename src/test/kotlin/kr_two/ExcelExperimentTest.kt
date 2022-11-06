package kr_two

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.formula.WorkbookEvaluator
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.test.Test


internal class ExcelExperimentTest {
    @Test
    @Throws(IOException::class)
    fun geekforgeeks() {
        val path = "src/main/kotlin/kr_two/JavaBooks.xls"

        val workbook = HSSFWorkbook()
        val sheet = workbook.createSheet("ChiSquareDistCalculator")

        val header = sheet.createRow(0)
        header.createCell(0).setCellValue("Уровень значимости альфа =")
        header.createCell(1).setCellValue("Число степеней свободы k =")
        header.createCell(2).setCellValue("Критическое хиКвадрат =")

        val row = sheet.createRow(1)

        row.createCell(0).setCellValue(0.01)
        row.createCell(1).setCellValue(107.toString())

        //CHISQ.INV.RT == ХИ2.ОБР.ПХ в русском экселе
        row.createCell(2).cellFormula = "_xlfn.CHISQ.INV.RT(A2,B2)"

        //К сожалению poi не поддерживает вычисление на лету CHISQ, поэтому придется сравнивать руками
        //workbook.creationHelper.createFormulaEvaluator().evaluateAll()
        println("Поддерживаемые формулы экселя = ${WorkbookEvaluator.getSupportedFunctionNames()}")

        FileOutputStream(path).use { fileOut ->
            workbook.write(fileOut)
            workbook.close()
            fileOut.close()
        }

        OSDetector.openWithSystem(File(path))

    }
}