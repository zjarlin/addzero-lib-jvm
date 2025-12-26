package site.addzero.gradle.plugin




plugins {
    id("site.addzero.gradle.plugin.kmp-convention")
}

kotlin {
    jvm()
    mingwX64()
    linuxX64()
    macosX64()
    macosArm64()
}