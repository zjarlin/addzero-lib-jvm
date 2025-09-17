@file:OptIn(ExperimentalForeignApi::class)

package site.addzero.inter

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.getenv

actual fun getPlatform(): Platform {
    return object : Platform {
        override val name: String = "mac"
    }
}

actual fun getUserHomeDirectory(): String {
    val getenv = getenv("HOME")?.toKString() ?:""
    return getenv
}

fun main() {
    val userHomeDirectory = getUserHomeDirectory()
    println(userHomeDirectory)
}
