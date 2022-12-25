package ipr_two

import java.util.prefs.Preferences

object IprTwoParamsSaver {
    private fun prefNode(): Preferences = Preferences.userRoot().node("IPR_TWO")

    fun loadIprTwoParams(): IprTwoParams {
        return prefNode().run {
            IprTwoParams(
                channelsN = getInt(IprTwoParams.N_KEY, 3),
                queueM = getInt(IprTwoParams.M_KEY, 10),
                inputFlowLambda = getDouble(IprTwoParams.LAMBDA_KEY, 1.0),
                serviceFlowMu = getDouble(IprTwoParams.MU_KEY, 1.0),
                leavingNu = getDouble(IprTwoParams.NU_KEY, 1.0)
            )
        }
    }

    fun saveIprTwoParams(params: IprTwoParams) {
        prefNode().apply {
            putInt(IprTwoParams.N_KEY, params.channelsN)
            putInt(IprTwoParams.M_KEY, params.queueM)
            putDouble(IprTwoParams.LAMBDA_KEY, params.inputFlowLambda)
            putDouble(IprTwoParams.MU_KEY, params.serviceFlowMu)
            putDouble(IprTwoParams.NU_KEY, params.leavingNu)
        }
    }
}

/**
 * @param inputFlowLambda λ интенсивность входящего потока
 * @param serviceFlowMu μ интенсивность потока обслуживания
 * @param leavingNu ν параметр закона ухода
 */
data class IprTwoParams(
    val channelsN: Int,
    val queueM: Int,
    val inputFlowLambda: Double,
    val serviceFlowMu: Double,
    val leavingNu: Double
) {
    companion object {
        const val N_KEY = "n"
        const val M_KEY = "m"
        const val LAMBDA_KEY = "λ"
        const val MU_KEY = "μ"
        const val NU_KEY = "ν"
    }
}
