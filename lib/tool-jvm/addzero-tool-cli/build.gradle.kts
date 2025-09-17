@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("kmp-json")
    id("kmp-koin-core")
    id("ksp4self")
}
kotlin {
    macosX64()
    macosArm64()
    mingwX64()
    linuxX64()
    linuxArm64()
    dependencies{
    }
}


//application {
//    // Define the Fully Qualified Name for the application main class
//    // (Note that Kotlin compiles `Main.kt` to a class with FQN `com.example.app.MainKt`.)
//    mainClass = "site.addzero.app.MainKt"
//}

