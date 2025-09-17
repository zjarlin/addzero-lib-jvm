@file:OptIn(ExperimentalForeignApi::class)

package site.addzero.inter

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.getenv

actual fun getPlatform(): site.addzero.inter.Platform {
    return object : Platform {
        override val name: String = "linux"
    }

}

actual fun getUserHomeDirectory(): String {
    val getenv = getenv("HOME")?.toKString() ?:""
    return getenv
}
