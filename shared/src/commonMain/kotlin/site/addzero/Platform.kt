package site.addzero

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
