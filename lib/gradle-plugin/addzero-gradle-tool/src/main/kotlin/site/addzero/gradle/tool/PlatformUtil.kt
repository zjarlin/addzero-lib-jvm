package site.addzero.gradle.tool

import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

fun KotlinMultiplatformExtension.doIos(nt: List<KotlinNativeTarget>) {
    nt.forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

}


fun KotlinMultiplatformExtension.defIos(): List<KotlinNativeTarget> {
    val listOf = listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    )
    return listOf

}

