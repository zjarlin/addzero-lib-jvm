package site.addzero.buildlogic.kmp

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

val libs = versionCatalogs.named("libs")
val javaVersion = libs.findVersion("jdk").get().requiredVersion
val jdk11Version = libs.findVersion("jdk11").get().requiredVersion.toInt()
val jvmTargetVersion = JvmTarget.fromTarget(jdk11Version.toString())

kotlin {
    jvmToolchain(maxOf(javaVersion.toInt(), jdk11Version))
    jvm {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(jvmTargetVersion)
                }
            }
        }
    }

    sourceSets {
        commonTest.dependencies {
            implementation(libs.findLibrary("org-jetbrains-kotlin-kotlin-test").get())
            implementation(libs.findLibrary("org-jetbrains-kotlinx-kotlinx-coroutines-test").get())
        }
    }
}
