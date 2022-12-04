package kr_two

import java.util.prefs.Preferences

object KrTwoParamsSaver {
    private fun prefNode(): Preferences = Preferences.userRoot().node("KR_TWO")

    fun loadKrTwoParams(): KrTwoParams {
        return prefNode().run {
            KrTwoParams(
                n = getInt(KrTwoParams.N_KEY, 2),
                mu = getDouble(KrTwoParams.MU_KEY, 2.0),
                RVN = getInt(KrTwoParams.RVN_KEY, 10000),
                realRandom = getBoolean(KrTwoParams.RANDOM_KEY, true),
                debug = getBoolean(KrTwoParams.DEBUG_KEY, false)
            )
        }
    }

    fun saveKrTwoParams(params: KrTwoParams) {
        prefNode().apply {
            putInt(KrTwoParams.N_KEY, params.n)
            putDouble(KrTwoParams.MU_KEY, params.mu)
            putInt(KrTwoParams.RVN_KEY, params.RVN)
            putBoolean(KrTwoParams.RANDOM_KEY, params.realRandom)
            putBoolean(KrTwoParams.DEBUG_KEY, params.debug)
        }
    }
}

data class KrTwoParams(
    val n: Int,
    val mu: Double,
    val RVN: Int,
    val realRandom: Boolean,
    val debug: Boolean
) {
    companion object {
        const val N_KEY = "n"
        const val MU_KEY = "mu"
        const val RVN_KEY = "rvn"
        const val RANDOM_KEY = "realRandom"
        const val DEBUG_KEY = "debug"
    }
}