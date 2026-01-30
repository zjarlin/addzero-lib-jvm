package site.addzero.buildlogic.kmp.libs

import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}
val libs = the<LibrariesForLibs>()


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
            implementation(libs.ksp.symbol.processing.api)
        }
    }
}


