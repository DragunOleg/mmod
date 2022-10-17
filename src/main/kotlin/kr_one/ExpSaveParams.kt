package kr_one

import java.util.prefs.Preferences

fun main() {
    val res = loadStoredParams()
    println("$res")
    saveParams(Params("QQ", "WW", "RR"))
    val res2 = loadStoredParams()
    println("$res2")
}

private fun prefNode(): Preferences = Preferences.userRoot().node("MMOD_STORAGE")

data class Params(val username: String, val password: String, val org: String)

fun loadStoredParams(): Params {
    return prefNode().run {
        Params(
            get("username", ""),
            get("password", ""),
            get("org", "kotlin"),
        )
    }
}

fun removeStoredParams() {
    prefNode().removeNode()
}

fun saveParams(params: Params) {
    prefNode().apply {
        put("username", params.username)
        put("password", params.password)
        put("org", params.org)
        sync()
    }
}