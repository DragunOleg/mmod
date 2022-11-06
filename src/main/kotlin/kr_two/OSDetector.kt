package kr_two

import java.awt.Desktop
import java.io.File
import java.util.*


object OSDetector {
    val isWindows: Boolean
    val isLinux: Boolean
    val isMac: Boolean
    init {
        val os = System.getProperty("os.name").lowercase(Locale.getDefault())
        isWindows = os.contains("win")
        isLinux = os.contains("nux") || os.contains("nix")
        isMac = os.contains("mac")
    }

    fun openWithSystem(file: File): Boolean {
        return try {
            if (isWindows) {
                Runtime.getRuntime().exec(
                    arrayOf(
                        "rundll32", "url.dll,FileProtocolHandler",
                        file.absolutePath
                    )
                )
                true
            } else if (isLinux || isMac) {
                Runtime.getRuntime().exec(
                    arrayOf(
                        "/usr/bin/open",
                        file.absolutePath
                    )
                )
                true
            } else {
                // Unknown OS, try with desktop
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file)
                    true
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace(System.err)
            false
        }
    }
}
