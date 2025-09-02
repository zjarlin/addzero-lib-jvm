package com.addzero.kaleidoscope.util

import java.io.File

object CodeUtil {

    fun genCode(pathname: String, code: String, skipExistFile: Boolean = false) {

        val targetFile = File(pathname)
        targetFile.parentFile?.mkdirs()
        if (skipExistFile) {
            if (targetFile.exists()) {
                return
            }
        }
        targetFile.writeText(code)
    }
}
