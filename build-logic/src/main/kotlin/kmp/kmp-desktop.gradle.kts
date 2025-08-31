import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("kmp-compose-common")

}
val libs = the<org.gradle.accessors.dm.LibrariesForLibs>()

kotlin {
    sourceSets {
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }
}

compose.desktop {
    application {
        mainClass = "io.gitee.zjarlin.addzero.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "io.gitee.zjarlin.addzero"
            packageVersion = "1.0.0"
        }
    }
}
