package site.addzero.gradle.plugin

import org.gradle.api.artifacts.VersionCatalogsExtension
import site.addzero.gradle.tool.lib
import site.addzero.gradle.tool.ver
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

val libs = the<VersionCatalogsExtension>().named("libs")

kotlin {
    jvmToolchain(8)

    targets.withType<KotlinJvmTarget>().configureEach {
        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_1_8)
                }
            }
        }
    }

    jvm()
    sourceSets {
        commonMain.dependencies {
            implementation(libs.lib("com-google-devtools-ksp-symbol-processing-api"))
        }
    }
}
