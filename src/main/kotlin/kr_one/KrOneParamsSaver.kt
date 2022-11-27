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