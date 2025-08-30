package io.gitee.zjarlin.addzero

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform