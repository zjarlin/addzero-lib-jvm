package site.addzero.buildlogic.kmp

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import site.addzero.gradle.BuildSettings

plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

val libs = versionCatalogs.named("libs")
val desktopPackageVersion = libs.findVersion("desktop-package-version").get().requiredVersion

kotlin {
    jvm()
    sourceSets {
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.findLibrary("org-jetbrains-kotlinx-kotlinx-coroutines-swing").get())
        }
    }
}

compose.desktop {
    application {
        mainClass = BuildSettings.Desktop.MAIN_CLASS

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = BuildSettings.PACKAGE_NAME
            packageVersion = desktopPackageVersion
        }
    }
}
