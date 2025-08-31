import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("kmp-compose-common")

}
val libs = the<org.gradle.accessors.dm.LibrariesForLibs>()

kotlin {
    jvm()
    sourceSets {
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.addzero.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = Vars.packageName

            packageVersion = "1.0.0"
        }
    }
}
