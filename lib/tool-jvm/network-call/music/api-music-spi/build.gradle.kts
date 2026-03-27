plugins {
    id("site.addzero.buildlogic.kmp.kmp-core")
    id("site.addzero.buildlogic.kmp.kmp-json")
}
val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        jvmMain.dependencies {
            implementation(libs.findLibrary("javazoom-jlayer").get())
        }
    }
}
