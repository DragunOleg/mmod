package ipr_one

import java.util.prefs.Preferences

object ParamsSaver {
    private fun prefNode(): Preferences = Preferences.userRoot().node("MMOD_STORAGE")

    fun loadIprOneParams(): IprOneParams {
        return prefNode().run {
            IprOneParams(
                get(IprOneParams.VECTOR_A_KEY,"8 5 3"),
                get(IprOneParams.VECTOR_B_KEY," 8 8")
            )
        }
    }
    fun saveIprOneParams(params: IprOneParams) {
        prefNode().apply {
            put(IprOneParams.VECTOR_A_KEY, params.vectorAString)
            put(IprOneParams.VECTOR_B_KEY, params.vectorBString)
        }
    }
}

data class IprOneParams(
    val vectorAString: String,
    val vectorBString: String
) {
    companion object{
        const val VECTOR_A_KEY = "vectorA"
        const val VECTOR_B_KEY = "vectorB"
    }
}