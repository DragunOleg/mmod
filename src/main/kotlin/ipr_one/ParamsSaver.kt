package ipr_one

import java.util.prefs.Preferences

object ParamsSaver {
    private fun prefNode(): Preferences = Preferences.userRoot().node("MMOD_STORAGE")

    fun loadIprOneParams(): IprOneParams {
        return prefNode().run {
            IprOneParams(
                n = getInt(IprOneParams.N_KEY, 2),
                m = getInt(IprOneParams.M_KEY, 2),
                matrix = get(IprOneParams.MATRIX_KEY, "1 2 3 4"),
                vectorAString = get(IprOneParams.VECTOR_A_KEY,"1 2"),
                vectorBString = get(IprOneParams.VECTOR_B_KEY," 3 4"),
                RVN = getInt(IprOneParams.RVN_KEY,200)
            )
        }
    }
    fun saveIprOneParams(params: IprOneParams) {
        prefNode().apply {
            putInt(IprOneParams.N_KEY, params.n)
            putInt(IprOneParams.M_KEY, params.m)
            put(IprOneParams.MATRIX_KEY, params.matrix)
            put(IprOneParams.VECTOR_A_KEY, params.vectorAString)
            put(IprOneParams.VECTOR_B_KEY, params.vectorBString)
            putInt(IprOneParams.RVN_KEY, params.RVN)
        }
    }
}

data class IprOneParams(
    val n: Int,
    val m: Int,
    val matrix: String,
    val vectorAString: String,
    val vectorBString: String,
    val RVN: Int
) {
    companion object{
        const val N_KEY = "n"
        const val M_KEY = "M"
        const val MATRIX_KEY = "matrix"
        const val VECTOR_A_KEY = "vectorA"
        const val VECTOR_B_KEY = "vectorB"
        const val RVN_KEY = "RVN"
    }
}