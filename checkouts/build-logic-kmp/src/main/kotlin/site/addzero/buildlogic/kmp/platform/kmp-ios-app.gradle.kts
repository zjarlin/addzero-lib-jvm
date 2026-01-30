package site.addzero.buildlogic.kmp.platform

plugins {
    id("org.jetbrains.kotlin.multiplatform")

}

kotlin {

       listOf(
        iosArm64(),
//        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

}

