package ipr_one

object InputValidator {

    fun validateString(userInput: String):List<Double> {
        println("validating $userInput")
        val ints = userInput.trim().split(" ").map { it.toInt() }
        if (ints.sum() != 16) throw Exception("Сумма должна быть равна 16")
        val result = ints.map { it.toDouble() / 16 }
        println("результат валидации и разложения: $result")
        return result
    }
}