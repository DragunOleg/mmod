package ipr_one

object InputValidator {

    fun validateInput(n: Int, m: Int, matrixString: String, vectorAString: String, vectorBString: String, RVN:Int) {
        val matrixWeights = matrixString.trim().split(" ")
        if (n == 0) throw Exception("n не может быть 0")
        if (m == 0) throw Exception("m не может быть 0")
        if (n*m != matrixWeights.size) throw Exception("размер матрицы не равен n*m")
        val vectorAList = vectorAString.trim().split(" ")
        val vectorBList = vectorBString.trim().split(" ")
        if (vectorAList.size != n) throw Exception("размер вектора А не равен n")
        if (vectorBList.size != m) throw Exception("размер вектора B не равен m")
        if (RVN == 0) throw Exception("Число случайных величин должно быть хотя бы 1")
    }
}