@file:OptIn(ExperimentalWasmDsl::class)

package site.addzero.gradle.plugin

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import site.addzero.gradle.BuildSettings

val libs = the<LibrariesForLibs>()

plugins {
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
    jvm()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("composeApp")
        binaries.executable()
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.lifecycle.runtime.compose)
            implementation(compose.materialIconsExtended)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }
    }
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

compose.desktop {
    application {
        mainClass = BuildSettings.Desktop.MAIN_CLASS

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = BuildSettings.PACKAGE_NAME

            packageVersion = "1.0.0"
        }
    }
}
