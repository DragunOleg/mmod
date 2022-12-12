package ipt_two

import java.util.prefs.Preferences

object IprTwoParamsSaver {
    private fun prefNode(): Preferences = Preferences.userRoot().node("IPR_TWO")


}

/**
 * @param inputFlowLambda λ интенсивность входящего потока
 * @param serviceFlowMu μ интенсивность потока обслуживания
 */
data class IprTwoParams(
    val channelsN: Int,
    val queueM: Int,
    val inputFlowLambda: Double,
    val serviceFlowMu: Double
)
