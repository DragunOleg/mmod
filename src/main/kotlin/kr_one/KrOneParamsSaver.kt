package kr_one

import java.util.prefs.Preferences

object KrOneParamsSaver {
    private fun prefNodeOne(): Preferences = Preferences.userRoot().node("KR_ONE_ONE")
    private fun prefNodeTwo(): Preferences = Preferences.userRoot().node("KR_ONE_TWO")
    private fun prefNodeThree(): Preferences = Preferences.userRoot().node("KR_ONE_THREE")
    private fun prefNodeFour(): Preferences = Preferences.userRoot().node("KR_ONE_FOUR")

    fun loadKrOneOneParams(): KrOneOneParams {
        return prefNodeOne().run {
            KrOneOneParams(
                Pa = getDouble(KrOneOneParams.PA_KEY, 0.5),
                n = get(KrOneOneParams.N_KEY, "5 100 1000 100000 1000000"),
                realRandom = getBoolean(KrOneOneParams.RANDOM_KEY, true),
                debug = getBoolean(KrOneOneParams.DEBUG_KEY, false)
            )
        }
    }

    fun saveKrOneOneParams(params: KrOneOneParams){
        prefNodeOne().apply {
            putDouble(KrOneOneParams.PA_KEY, params.Pa)
            put(KrOneOneParams.N_KEY, params.n)
            putBoolean(KrOneOneParams.RANDOM_KEY, params.realRandom)
            putBoolean(KrOneOneParams.DEBUG_KEY, params.debug)
        }
    }

    fun loadKrOneTwoParams(): KrOneTwoParams {
        return prefNodeTwo().run {
            KrOneTwoParams(
                Pa = getDouble(KrOneTwoParams.PA_KEY, 0.5),
                Pb = getDouble(KrOneTwoParams.PB_KEY, 0.5),
                n = getInt(KrOneTwoParams.N_KEY, 10),
                realRandom = getBoolean(KrOneTwoParams.RANDOM_KEY, true),
                debug = getBoolean(KrOneTwoParams.DEBUG_KEY, false),
                valuesDraw = getBoolean(KrOneTwoParams.VALUES_DRAW_KEY, true)
            )
        }
    }

    fun saveKrOneTwoParams(params: KrOneTwoParams) {
        prefNodeTwo().apply {
            putDouble(KrOneTwoParams.PA_KEY, params.Pa)
            putDouble(KrOneTwoParams.PB_KEY, params.Pb)
            putInt(KrOneTwoParams.N_KEY, params.n)
            putBoolean(KrOneTwoParams.RANDOM_KEY, params.realRandom)
            putBoolean(KrOneTwoParams.DEBUG_KEY, params.debug)
            putBoolean(KrOneTwoParams.VALUES_DRAW_KEY, params.valuesDraw)
        }
    }

}

data class KrOneOneParams(
    val Pa: Double,
    val n: String,
    val realRandom: Boolean,
    val debug: Boolean
) {
    companion object {
        const val PA_KEY = "Pa"
        const val N_KEY = "n"
        const val RANDOM_KEY = "realRandom"
        const val DEBUG_KEY = "debug"
    }
}

data class KrOneTwoParams(
    val Pa: Double,
    val Pb: Double,
    val n: Int,
    val realRandom: Boolean,
    val debug: Boolean,
    val valuesDraw: Boolean
) {
    companion object {
        const val PA_KEY = "Pa"
        const val PB_KEY = "Pb"
        const val N_KEY = "n"
        const val RANDOM_KEY = "realRandom"
        const val DEBUG_KEY = "debug"
        const val VALUES_DRAW_KEY = "drawValues"
    }
}